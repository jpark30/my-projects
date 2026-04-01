package edu.wm.cs.cs301.s2026.wordle.model;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import edu.wm.cs.cs301.s2026.wordle.controller.ReadWordsRunnable;

/**
 * Abstract base class for Wordle game models implementing the Template Method Pattern.
 *
 * <p>This class contains all shared game state and logic between WordleModel
 * and AbsurdleModel. It implements the setCurrentRow() template method whose
 * overall structure is the same for both strategies. The step that determines
 * cell colors is delegated to the abstract primitive method evaluateColors()
 * which each subclass implements differently.</p>
 */
public abstract class AbstractModel implements Model {

    /** The target word, stored as uppercase char array. */
    protected char[] currentWord;

    /** The player's current guess, stored as a char array. */
    protected char[] guess;

    /** Number of letters per word. Always 5. */
    protected final int columnCount;

    /** Maximum number of guess rows. Always 6. */
    protected final int maximumRows;

    /** Current column index. Range -1 to columnCount - 1. */
    protected int currentColumn;

    /** Current row index. Range 0 to maximumRows. */
    protected int currentRow;

    /** List of all valid words for the game. */
    protected List<String> wordList;

    /** Random number generator for word selection. */
    protected final Random random;

    /** Tracks cumulative game statistics. */
    protected final Statistics statistics;

    /** The 2D grid of WordleResponse objects. */
    protected WordleResponse[][] wordleGrid;

    /** Latch used to wait for asynchronous word list loading. */
    protected final CountDownLatch wordListLatch;

    /**
     * No-parameter constructor that initializes all shared fields.
     * Starts the background thread to load the word list.
     */
    public AbstractModel() {
        currentColumn = -1;
        currentRow = 0;
        columnCount = 5;
        maximumRows = 6;
        random = new Random();
        wordListLatch = new CountDownLatch(1);

        createWordList();

        wordleGrid = initializeWordleGrid();
        guess = new char[columnCount];
        statistics = new Statistics();
    }

    /**
     * Starts a background thread to load the word list asynchronously.
     */
    private void createWordList() {
        ReadWordsRunnable runnable = new ReadWordsRunnable(this);
        new Thread(runnable).start();
    }
    
    /**
     * Sets the word list and releases the latch to unblock generateCurrentWord().
     * Called by ReadWordsRunnable when loading is complete.
     *
     * @param wordList the list of valid words loaded from disk
     */
    @Override
    public void setWordList(List<String> wordList) {
        this.wordList = wordList;
        wordListLatch.countDown();
    }

    /**
     * Resets grid, row/column counters, and guess array.
     * Subclasses must call super.initialize() or replicate
     * this logic and add their own reset behavior.
     */
    @Override
    public void initialize() {
        wordleGrid = initializeWordleGrid();
        currentColumn = -1;
        currentRow = 0;
        guess = new char[columnCount];
    }

    /**
     * Checks if the current guess is a valid word in the word list.
     *
     * @return true if the guess matches a word in the list, false otherwise
     */
    @Override
    public boolean isValidGuess() {
        String guessAsString = new String(guess).trim();
        for (String word : wordList) {
            if (word.equalsIgnoreCase(guessAsString)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the target word as an uppercase string.
     * Null case included for Absurdle.
     * @return currentWord as an uppercase string
     */
    @Override
    public String getCurrentWord() {
    	if (currentWord == null) {
            return "";
        }
        return new String(currentWord);
    }

    /**
     * Sets the target word directly. Intended for testing purposes.
     *
     * @param word the target word to set
     */
    @Override
    public void setCurrentWord(String word) {
        currentWord = word.toUpperCase().toCharArray();
    }

    /**
     * Records a typed letter into the current column of the active row.
     * Increments currentColumn and clamps it at columnCount - 1.
     *
     * @param c the character typed by the player
     */
    @Override
    public void setCurrentColumn(char c) {
    	if (currentColumn < columnCount -1) {
    		currentColumn++;
            currentColumn = Math.min(currentColumn, columnCount - 1);
            guess[currentColumn] = c;
            wordleGrid[currentRow][currentColumn] = new WordleResponse(c,
                    Color.WHITE, Color.BLACK);
    	}
    }

    /**
     * Removes the most recently typed letter.
     * Clamps currentColumn at -1 to prevent negative indexing.
     */
    @Override
    public void backspace() {
        wordleGrid[currentRow][currentColumn] = null;
        guess[currentColumn] = ' ';
        currentColumn--;
        currentColumn = Math.max(currentColumn, -1);
    }

    /**
     * Returns the array of WordleResponse objects for the most recently submitted row.
     *
     * @return the array of WordleResponse for the most recent row
     */
    @Override
    public WordleResponse[] getCurrentRow() {
        return wordleGrid[getCurrentRowNumber()];
    }

    /**
     * Returns the index of the most recently submitted row.
     *
     * @return currentRow - 1
     */
    @Override
    public int getCurrentRowNumber() {
        return currentRow - 1;
    }

    /**
     * Template method that evaluates the current guess and updates the grid.
     *
     * <p>This is the TEMPLATE METHOD. Its overall structure is the same for
     * both WordleModel and AbsurdleModel:
     * 1. Validate the guess
     * 2. Evaluate colors — delegated to evaluateColors() (the primitive method)
     * 3. Update the grid with the evaluated colors
     * 4. Reset column, increment row, clear guess
     * 5. Return whether rows remain
     *
     * Step 2 is the only step that differs between strategies and is
     * therefore declared abstract and implemented by each subclass.</p>
     *
     * @return true if there are remaining guess rows, false otherwise
     */
    @Override
    public final boolean setCurrentRow() {

        Color[] backgroundColors = evaluateColors();
        for (int column = 0; column < guess.length; column++) {
            wordleGrid[currentRow][column] = new WordleResponse(
                    guess[column], backgroundColors[column], Color.WHITE);
        }
        currentColumn = -1;
        currentRow++;
        guess = new char[columnCount];
        
        return currentRow < maximumRows;
    }

    /**
     * Abstract primitive method that determines the background color for each
     * letter in the current guess.
     *
     * <p>This is the PRIMITIVE METHOD of the Template Method Pattern.
     * WordleModel implements this by comparing the guess against a fixed
     * target word. AbsurdleModel implements this adversarially by choosing
     * the color pattern that leaves the most words remaining.</p>
     *
     * @return an array of Colors of length columnCount, one per letter
     */
    protected abstract Color[] evaluateColors();

    /**
     * Creates and returns a maximumRows x columnCount grid with all cells null.
     *
     * @return a new 2D array of WordleResponse objects, all null
     */
    protected WordleResponse[][] initializeWordleGrid() {
        WordleResponse[][] grid = new WordleResponse[maximumRows][columnCount];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = null;
            }
        }
        return grid;
    }

    /**
     * Returns a random valid index into the word list.
     *
     * @return a random integer in range [0, wordList.size())
     */
    protected int getRandomIndex() {
        return random.nextInt(wordList.size());
    }
    @Override
    public int getTotalGamesWon() {
    	return statistics.getTotalGamesWon();
    }
    @Override
    public int getLastWin() {
    	return statistics.getLastWin();
    }
    @Override
    public int[] calculateArrayOfWins() {
    	return statistics.calculateArrayOfWins(maximumRows);
    }
    @Override
    public void saveDataToFile() {
        statistics.writeStatistics();
    }
    @Override
    public int getTotalGamesPlayed() {
        return statistics.getTotalGamesPlayed();
    }
    @Override
    public int getCurrentStreak() {
        return statistics.getCurrentStreak();
    }
    @Override
    public int getLongestStreak() {
        return statistics.getLongestStreak();
    }
    
    protected Statistics getStatistics() { return statistics; }

    @Override
    public WordleResponse[][] getWordleGrid() { return wordleGrid; }

    @Override
    public int getMaximumRows() { return maximumRows; }

    @Override
    public int getColumnCount() { return columnCount; }

    @Override
    public int getCurrentColumn() { return currentColumn; }

    @Override
    public int getTotalWordCount() {
        if (wordList == null) return 0;
        return wordList.size();
    }
    
    @Override
    public int getRemainingWordCount() {
    	return getTotalWordCount();
    }

    @Override
    public void incrementTotalGamesPlayed() {
        statistics.incrementTotalGamesPlayed();
    }

    @Override
    public void addWordsGuessed(int rowNumber) {
        statistics.addWordsGuessed(rowNumber);
    }

    @Override
    public void setCurrentStreak(int streak) {
        statistics.setCurrentStreak(streak);
    }

    @Override
    public void incrementCurrentStreak() {
        statistics.setCurrentStreak(statistics.getCurrentStreak() + 1);
    }
    
    
}
