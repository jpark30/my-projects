#ifndef __CSCI303_DIJKSTRA_HPP__
#define __CSCI303_DIJKSTRA_HPP__

#include "graph.hpp"

namespace csci303 {
  template <class T>
  std::tuple<std::vector<double>, std::vector<Vertex>> dijkstra(const Graph<T> &G, const Vertex &s) {
    /*
     * In the weighted graph G (directed or undirected), find the shortest path tree rooted at s.
     *
     * The lazy Dijkstra algorithm:
     *   1. Push the start vertex onto the queue.
     *   2. While the queue is not empty:
     *      - Pop the vertex v closest to the start node via the shortest path tree.
     *      - Relax the vertices adjacent to v.  If we reduce the current best distance 
     *        from a vertex to the source we enqueue the new value.
     */

    /*
     * On return,
     *   dist[] is a std::vector<double> of shortest path distances, andn
     *   prev[] is a std::vector<Vertex> where prev[v] is the vertex that precedes v
     *   on a shortest path from the root.
     */

     struct foo {//REPRESENTED AS (distance, vector)
      foo(const int &n, const double &x){
        my_n = n;
        my_x = x;

      }
      Vertex my_n;//Vertex
      double my_x;//Distance
     };

    double inf = 1.0/0.0;  // Set this to infinity.
    std::vector<double> dist(G.V(), inf);
    std::vector<Vertex> prev(G.V(), -1);
    std::vector<bool> spt(G.V(), false); // A vector that tells you whether a vertex is in the Shortest paths tree. Also used to track visitation.

    auto compare = [](foo e, foo f) {return (e.my_x > f.my_x);};//A lambda function copied from MST project, it will compare the struct foo by comparing 'my_x' which is the distance
    std::priority_queue<foo, std::vector<foo>, decltype(compare)> pq(compare);// Priority queue declaration copied from MST project, changed typenames to foo.

    //Initialize source vertex's distance and previous vertex(itself)
    dist[s] = 0;
    prev[s] = s;


    foo dinosaur(s,0);//Push source
    pq.push(dinosaur);
  
    while(pq.size() != 0){//While the priority queue isn't empty
      auto v = pq.top();//Grab the shorted distance vector pair
      if (spt[v.my_n] != true){//If it hasn't been visited
        spt[v.my_n] = true;//Mark it visited
        for (auto w : G.adj[v.my_n]){//For every node connected to that node(In this case 'w' is an edge, but we get the other node via w.other(v.my_n))
          if (dist[w.other(v.my_n)] > v.my_x + w.weight()){//If the new path is shorter than the current best known path
            dist[w.other(v.my_n)] = v.my_x + w.weight();//Set it as the shortest path
            prev[w.other(v.my_n)] = v.my_n;//Mark the previous node
            foo dinosaur(w.other(v.my_n),dist[w.other(v.my_n)]);//Initialize it as a distance vector pair
            pq.push(dinosaur);
          }
        }
      }
      else{//If it has been visited, pop it.
        pq.pop();
      }
    }

    return std::tuple(dist, prev);
  }
}
#endif
