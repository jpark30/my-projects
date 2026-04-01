package edu.wm.cs.cs301.s2026.wordle.model;

import java.util.List;

public interface Model {

	/**
	 * Initializes the Wordle Grid. Default values for the current column and row are set. 
	 * The target word and the user guess are initialized. currentColumn is set to -1 and 
	 * currentRow is set to 0. Should only be called if the word list has been initialized
	 */
	void initialize();

	/**
	 * Checks if the current guess exists in the word list.
	 * Comparison is case-insensitive since currentWord is stored uppercase
	 * but wordList may contain lowercase words.
	 * @return true if the guess is a valid word in the word list, false otherwise
	 */
	boolean isValidGuess();

	/**
	 * Generates the word to be guessed. The chosen word is converted to uppercase.
	 * Comes with a print statement for testing purposes. The function can be used to set the target word as well.
	 */
	void generateCurrentWord();

	/**
	 * Retrieves the target word, returning it as an uppercase string.
	 * @return currentWord as an uppercase string
	 */
	String getCurrentWord();

	/**
	 * Setter function for wordList. Used by ReadWordsRunnable.java
	 * @param wordList List of all words for the Wordle game.
	 */
	void setWordList(List<String> wordList);

	/**
	 * This method updates the wordleGrid so that the user input is visible.
	 * Math.min ensures that the currentColumn index does not go out of bounds(columnCount -1).
	 * The user guess is stored with the parameter c.
	 * The wordleGrid is updated with the typed letter.
	 * @param c the character that the user has typed
	 */
	void setCurrentColumn(char c);

	/**
	 * Replaces the character that the user has most recently input with null.
	 * currentColumn is set to a minimum of -1 to prevent negative indexing.
	 */
	void backspace();

	/**
	 * Returns the array of WordleResponse for the most recent row
	 * @return return the array of WordleResponse for the most recent row
	 */
	WordleResponse[] getCurrentRow();

	/**
	 * Returns the number associated with the row.
	 * Notice how it is decremented by 1 due to an increment in setCurrentRow.
	 * @return The current number of the row
	 */
	int getCurrentRowNumber();

	/**
	 * This method evaluates user's guess with the target word.
	 * If the guess in not valid, reject the guess by returning true
	 * For each letter of the user's guess, if it is in the right position, mark it green,
	 * yellow if in the wrong position, and gray if not in the word.
	 * The wordleGrid is then updated with the user's guess, with corresponding correct color.
	 * The method then sets the column to the starting position of -1 (currentColumn) and starts on a new row (CurrentRow)
	 * @return A boolean that returns true if the user has guesses remaining, false otherwise
	 */
	boolean setCurrentRow();
	
	/**
	 * Returns number of possible words in the wordlist.
	 */
	int getRemainingWordCount();
	/**
	 * Returns a grid representing the Wordle grid. Each square has a letter and color, or an empty space.
	 * @return The Wordle grid with dimensions maximumRows * columnCount
	 */

	WordleResponse[][] getWordleGrid();
	/**
	 * Sets the target word for both games. For testing purposes
	 * @param word the desired target word
	 */
	public void setCurrentWord(String word);
	/**
	 * Retrieves the number of maximum rows
	 * @return Number of maximum rows
	 */
	int getMaximumRows();

	/**
	 * Retrieves the column count
	 * @return The count, default is 5
	 */
	int getColumnCount();

	/**
	 * Retrieves the current column
	 * @return The current column used by setCurrentRow and setCurrentColumn
	 */
	int getCurrentColumn();

	/**
	 * Retrieves the number of possible words
	 * @return The size of the string array wordList
	 */
	int getTotalWordCount();


	void incrementTotalGamesPlayed();

	void addWordsGuessed(int growNumber);

	int getCurrentStreak();

	void setCurrentStreak(int streak);

	void incrementCurrentStreak();
	
	void saveDataToFile();
	
	int getTotalGamesWon();
	
	int getLastWin();
	
	int[] calculateArrayOfWins();
	
	int getTotalGamesPlayed();
	
	int getLongestStreak();

}