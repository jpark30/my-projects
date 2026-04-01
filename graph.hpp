#ifndef __RML_GRAPH_HPP__
#define __RML_GRAPH_HPP__
#include <algorithm>
#include <concepts>
#include <iostream>
#include <map>
#include <queue>
#include <random>
#include <stdexcept>
#include <tuple>
#include <type_traits>
#include <unordered_set>
#include <vector>

/***************************************************************************************************************
 * Design notes:
 * - I have tried to keep the overall design as simple as possible.  In particular, I avoid
 *   having more than one Graph class---mixing inheritance and templating is asking for trouble.
 *   See https://isocpp.org/wiki/faq/templates#nondependent-name-lookup-members.
 *
 *   I differentiate between different types of graphs purely through the types of edges
 *   they have.
 *
 *   Here are some design decisions that I found bigly simplified the code:
 *    - Undirected edges are implemented as if they were a pair of arcs in opposite directions.
 *      However, an undirected edge only appears once in the list of edges.
 *
 *    - All edges and arcs are weighted.  Unweighted edges have a hidden weight of 1.  This enables
 *      the application of algorithms like Bellman-Ford and Floyd-Warshall to unweighted graphs.
 *
 *    - I explicitly keep the entire adjacency matrices of undirected graphs, even though it is
 *      symmetric.
 *   As a consequence my design is not as parsimonious with memory as it could be, but it does
 *   facilitate code reuse and simplifies design.
 *
 * - I expect the vertex labels to be integers without gaps.
 *
 *   I did not place these classes in their own namespace because this would complicate being able
 *   to have friends in both the csci303 and rml namespaces.  It would not be impossible (?), but it
 *   would be a real rectalgia.
 *
 * To do:
 * - Handle multigraphs.
 * - Investigate alternatives to my assumptions on vertex labels so that I can handle general
 *   subgraphs.  Maps should do the trick, at some extra computational cost.
 */

/***************************************************************************************************************
 * Some forward declarations.
 */

// Vertices are labeled with long ints.
using lint = long int;  // Make lint an alias for long int.
using ulint = unsigned long int;  // Make ulint an alias for unsigned long int.
using Vertex = long int;  // Make Vertex an alias for long int.

// We use Paths to return the distance and path information from the shortest paths algorithms.
using Paths = std::tuple<std::vector<double>, std::vector<Vertex>>;

enum Weights {
  rando,    // Random real numbers from Uniform(0,1).
  distinct  // Distinct integers.
};

/*
 * More forward declarations so that I can
 *  - declare these friends of Graph to be in another namespace, and
 *  - specialize these functions to the same type T as the Graph they are friends with.
 *
 * First, some C++20 concepts.  These are constraints we can place on template parameters
 * to detect invalid template types early in the compilation process.  I need to place them
 * way up here to prevent (in g++)
 *    error: requires clause differs in template redeclaration
 * when I apply them to the Graph class.  The problem arises because the "requires" specifier
 * needs to be consistant across all declarations of Graph, both the forward ones and the one
 * where Graph is defined.
 */

class Edge;  // Forward declarations so I can refer to these classes in the concepts.
class Weighted_Edge;
class Arc;
class Weighted_Arc;

template <class T> concept is_undirected = std::same_as<T, Edge> or std::same_as<T, Weighted_Edge>;
template <class T> concept is_directed = std::same_as<T, Arc> or std::same_as<T, Weighted_Arc>;
template <class T> concept is_unweighted = std::same_as<T, Edge> or std::same_as<T, Arc>;
template <class T> concept is_weighted = std::same_as<T, Weighted_Edge> or std::same_as<T, Weighted_Arc>;

template <typename T> requires is_undirected<T> or is_directed<T> class Graph;  // Another forward reference.

namespace csci303 {  // For the student versions of the functions.
  template <typename T> Paths bfs(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths dfs(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths dijkstra(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths bellman_ford(const Graph<T> &G, const Vertex &start);
  template <typename T> std::vector<double> floyd_warshall(const Graph<T> &G);
  Graph<Weighted_Edge> prim(const Graph<Weighted_Edge> &G, const Vertex &s);
  Graph<Weighted_Edge> kruskal(const Graph<Weighted_Edge> &G);  
}

namespace rml {  // For my reference implementations.
  template <typename T> Paths bfs(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths dfs(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths dijkstra(const Graph<T> &G, const Vertex &start);
  template <typename T> Paths bellman_ford(const Graph<T> &G, const Vertex &start);
  template <typename T> std::vector<double> floyd_warshall(const Graph<T> &G);
  Graph<Weighted_Edge> prim(const Graph<Weighted_Edge> &G, const Vertex &s);
  Graph<Weighted_Edge> kruskal(const Graph<Weighted_Edge> &G);  
}

template <typename T>
Graph<T> set_weights(Graph<T> &G, enum Weights type, unsigned int seed);

/***************************************************************************************************************
 * Edges.
 */

/**************************************
 * An unweighted, undirected edge.
 */
  
class Edge
{
public:
  Edge() {}  // Default constructor.
  Edge(Vertex u, Vertex v) : u{u}, v{v} {}
    
  inline auto endpoints() const {return std::tuple(u, v);}  // Return both endpoints of an edge.
  inline Vertex either() const {return u;}        // Return one endpoint of an edge.
  inline Vertex other(const Vertex vertex) const  // Return the endpoint other than "vertex".
  {
    if (vertex == u) return v;
    if (vertex == v) return u;
    throw std::runtime_error("Invalid edge!"); // Throw an exception if vertex isn't an endpoint.
  }
  
  // The following two functions are required when creating a std::unordered_set of edges.
  bool operator==(const Edge &e) const {
    // The == operator does not distinguish between distinct edges with the same endpoints.
    // This would need to be addressed if we generalize to multigraphs.
    return (((u == e.u) and (v == e.v)) or ((v == e.u) and (u == e.v)));
  }
  
  std::size_t hash() const {
    // I do not remember how I chose this hash function...
    return ((u + v) * (u + v + 1))/2;
  }

  // The "template <typename T>" is needed so that templated versions of these operators
  // are substantiated.
  template <typename T> friend std::istream &operator>>(std::istream &is, Edge &e);
  template <typename T> friend std::ostream &operator<<(std::ostream &os, const Edge &e);

  std::istream &read(std::istream &is) {
    is >> u >> v;
    return is;
  }

  std::ostream &write(std::ostream &os) const  {
    os << u << '-' << v;
    return os;
  }

protected:
  Vertex u, v;  // The endpoints of the edge.
  double wt = 1.0;  // A default weight so we can apply MST, Dijkstra, et al., to undirected graphs.
};

// We need to make our Edge hash function visible in the std namespace so it can be used
// by the C++ set classes.
namespace std {
  // This template is restricted to an edge or arc type.
  template <typename T> requires is_undirected<T> or is_directed<T> 
  struct hash<T> {
    std::size_t operator() (const T &e) const {
      return e.hash();
    }
  };
}

/************************************
 * A weighted undirected edge.
 */

class Weighted_Edge : public Edge
{
public:
  Weighted_Edge() {}
  Weighted_Edge(Vertex u, Vertex v) : Edge(u,v) {}
  
  inline double weight() const {return wt;} // Return the weight of the edge.
  inline void set_weight(const double &weight) {wt = weight;} // Set the weight.

  // template <typename T>
  template <typename T> friend std::istream &operator>>(std::istream &is, Weighted_Edge &e);    
  template <typename T> friend std::ostream &operator<<(std::ostream &os, const Weighted_Edge &e);    

  std::istream &read(std::istream &is) {
    is >> u >> v >> wt;
    return is;
  }
  
  //  template <typename T>
  std::ostream &write(std::ostream &os) const {
    os << u << '-' << v << "(" << wt << ')';
    return os;
  }
};

/***********************************************
 * An arc (a directed, unweighted edge).
 */

class Arc: public Edge
{
public:
  Arc() {}
  Arc(Vertex u, Vertex v) : Edge(u,v) {}

  inline Vertex from() const {return u;}  // The tail of the arc.
  inline Vertex to() const {return v;}    // The head of the arc.
    
  // The == operator does not distinguish between distinct edges with the same endpoints.
  // This would need to be addressed if we generalize to multigraphs.
  bool operator==(const Arc &e) const {
    return ((u == e.u) and (v == e.v));
  }

  template <typename T> friend std::istream &operator>>(std::istream &is, Arc &e);
  template <typename T> friend std::ostream &operator<<(std::ostream &os, const Arc &e);  
  
  std::ostream &write(std::ostream &os) const {
    os << u << "->" << v;
    return os;
  }
};

/****************************
 * A weighted arc.
 */

// Because I derive Weighted_Arc from Edge rather than Arc there is some duplication of code.
// However, deriving from Edge avoids all manner of C++ complexity.  In particular, getting
// at the Edge constructor causes lots of trouble if I derive from Arc.

class Weighted_Arc : public Edge
{
public:
  Weighted_Arc() {}
  Weighted_Arc(Vertex u, Vertex v) : Edge(u,v) {}

  inline Vertex from() const {return u;}  // The tail of the arc.
  inline Vertex to() const {return v;}    // The head of the arc.
  inline double weight() const {return wt;}  // Return the weight of the edge.
  inline void set_weight(double weight) {wt = weight;} // Set the weight.

  // The == operator does not distinguish between distinct edges with the same endpoints.
  // This would need to be addressed if we generalize to multigraphs.
  bool operator==(const Weighted_Arc &e) const {
    return ((u == e.u) and (v == e.v));
  }
  
  template <typename T> friend std::istream &operator>>(std::istream &is, Weighted_Arc &e);    
  template <typename T> friend std::ostream &operator<<(std::ostream &os, const Weighted_Arc &e);
  
  std::ostream &write(std::ostream &os) const {
    os << u << "->" << v << "(" << wt << ')';
    return os;
  }
};

/******************************************************************************************
 * If I don't use the "require" concept here the compiler will attempt to instantiate
 * these functions for *all* i/o!  That is, a simple
 *     std::cout << "Hello, world!"
 * will instantiate the operator<< here which will fail---'zounds!
 */

template <typename T> requires is_undirected<T> or is_directed<T>
std::istream &operator>>(std::istream &is, T &e)
{
  // Using a templated operator>> allows me to avoid virtual functions and the "virtual friends" idiom.
  return e.read(is);
}

template <typename T> requires is_undirected<T> or is_directed<T>
std::ostream &operator<<(std::ostream &os, const T &e)
{
  // Likewise, a templated operator<< allows me to avoid the "virtual friends" idiom.  
  return e.write(os);
}

/***************************************************************************************************************
 * The graph class.
 *   Graph<Edge>          => an unweighted, undirected graph.
 *   Graph<Weighted_Edge> => a    weighted, undirected graph.
 *   Graph<Arc>           => an unweighted,   directed graph.
 *   Graph<Weighted_Arc>  => a    weighted,   directed graph.
 */

template <typename T> requires is_undirected<T> or is_directed<T>  // Fail fast if a bogus T is specified.
class Graph
{
public:
  Graph(ulint V=0) : adj(V) {}  // We have to initialize the vector adj[] this way as it is an object.

  // Copy constructor.  
  Graph(const Graph &G)
  {
    adj = G.adj;
    vertices = G.vertices;
    edges = G.edges;
  }

  // move constructor.  
  Graph(const Graph &&G)
  {
    adj = std::move(G.adj);
    vertices = std::move(G.vertices);    
    edges = std::move(G.edges);
  }

  // Copy assignment.
  Graph &operator=(const Graph &lhs)
  {
    adj = lhs.adj;
    vertices = lhs.vertices;
    edges = lhs.edges;
    return *this;
  }

  // Move assignment.
  Graph &operator=(const Graph &&lhs)
  {
    adj = std::move(lhs.adj);
    vertices = std::move(lhs.vertices);        
    edges = std::move(lhs.edges);
    return *this;
  }
  
  inline auto E() const {return edges.size();}
  inline auto V() const {return vertices.size();}

  // Regretably, there is no easy way to make variables read-only in C++,
  // so we will return copies.
  auto get_adj()   {return adj;}
  auto get_edges() {return edges;}

  inline void add_vertex(const Vertex &v) {vertices.insert(v);}
  inline void add_vertices(const Vertex &u, const Vertex &v) {
    // Nothing happens if u or v is already present in vertices[].
    vertices.insert(u);
    vertices.insert(v);
  }  

  friend void add_edge(Graph &G, const T &e)
  {
    // Add an edge or arc to a graph.
    auto [u, v] = e.endpoints();  // Assign u,v the contents of a two-ple.

    // Add the endpoints to the set of vertices.
    G.add_vertices(u, v);

    // Check whether either vertex number exceeds the current V.
    // If so, resize the vector of adjacency vectors.
    ulint m = std::max(u, v) + 1;
    if (m > G.adj.size()) {
      G.adj.resize(m);
    }

    // Add the edge (u,v) to the adjacency vector of u.
    G.adj[u].push_back(e);

    // For an undirected graph, add the edge (v,u) to the adjacency vector of v.    
    if (std::same_as<T, Edge> or std::same_as<T, Weighted_Edge>) {
      G.adj[v].push_back(e);      
    }

    // Add the edge to the vector of edges.
    G.edges.push_back(e);
  }

  // Set the weights in weighted graphs.
  friend Graph set_weights(const Graph &G, enum Weights type=distinct, unsigned int seed=std::random_device{}())
    requires is_weighted<T>
  {
    Graph G_new(G.V());
    std::mt19937_64 rng(seed);
    std::uniform_real_distribution<double> ur(0,1);
    unsigned long int wt = 1;  // This way I can use total weights to check the student MSTs and SPTs.

    if (type == rando) {
      for (T e : G.edges) {
        e.set_weight(ur(rng));
        add_edge(G_new, e);
      }
    }
    else {
      for (T e : G.edges) {    
        e.set_weight(wt++);
        add_edge(G_new, e);
      }      
    }
    return G_new;
  }

  // Input operator.
  friend std::istream &operator>>(std::istream &is, Graph &G)
  {
    // Read an edge and add it to G.
    T e;
    while (is >> e) {
      add_edge(G, e);
    }
    return is;
  }

  // Output operator.
  friend std::ostream &operator<<(std::ostream &os, const Graph &G)
  {
    os << " E: " << G.E() << '\n';
    os << " V: " << G.V() << '\n';
    os << " edge list = {" << '\n' << "  ";
    for (const T &e : G.edges) {
      os << e << ' ';
    }
    os << '\n' << " }" << std::endl;
    os << " adjacency list = {" << '\n';
    for (Vertex v = 0; v < G.V(); v++) {
      os << "  " << v << ": ";
      for (const T &e : G.adj[v]) {
        os << e << ' ';
      }
      os << std::endl;
    }
    os << " }" << std::endl;
      
    return os;
  }

  // Graph algorithms.
  friend Paths csci303::bfs<>(const Graph<T> &G, const Vertex &start);  
  friend Paths csci303::dfs<>(const Graph<T> &G, const Vertex &start);
  friend Paths csci303::dijkstra<>(const Graph<T> &G, const Vertex &start);
  friend Paths csci303::bellman_ford<>(const Graph<T> &G, const Vertex &start);
  friend std::vector<double> csci303::floyd_warshall<>(const Graph<T> &G);
  friend Graph<Weighted_Edge> csci303::prim(const Graph<Weighted_Edge> &G, const Vertex &s);
  friend Graph<Weighted_Edge> csci303::kruskal(const Graph<Weighted_Edge> &G);

  friend Paths rml::bfs<>(const Graph<T> &G, const Vertex &start);  
  friend Paths rml::dfs<>(const Graph<T> &G, const Vertex &start);
  friend Paths rml::dijkstra<>(const Graph<T> &G, const Vertex &start);
  friend Paths rml::bellman_ford<>(const Graph<T> &G, const Vertex &start);
  friend std::vector<double> rml::floyd_warshall<>(const Graph<T> &G);
  friend Graph<Weighted_Edge> rml::prim(const Graph<Weighted_Edge> &G, const Vertex &s);
  friend Graph<Weighted_Edge> rml::kruskal(const Graph<Weighted_Edge> &G);
  
protected:
  std::vector<std::vector<T>> adj;  // The adjacency matrix; adj[v] is a vector of edges.
  std::unordered_set<Vertex> vertices;  // The vertices.
  std::vector<T> edges;  // The edges.  
};
#endif
