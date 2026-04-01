package edu.wm.cs.cs301.s2026.wordle.model;

import java.awt.Color;

/**
 * This class manages the Wordle game state and logic. 
 */
public class WordleModel extends AbstractModel {
	/**
	 * No parameter constructor inherited from the AbstractModel class.
	 */
	public WordleModel() {
		super();
	}

	/**
	 * Overriding Initialize from the AbstractModel Class.
	 */
	@Override
	public void initialize() {
		super.initialize();
		generateCurrentWord();
	}
	/**
	 * Creates the word to be guessed. Set to run after word list is initialized.
	 */
	@Override
	public void generateCurrentWord() {
		try {
	        wordListLatch.await();  // blocks only if the word list is not ready yet
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        System.err.println("Interrupted while waiting for word list: " + e.getMessage());
	    }
		String word = wordList.get(getRandomIndex());
//		String word = "TRACE";
		currentWord = word.toUpperCase().toCharArray();
		System.out.println("To help with testing, word is " + word);
	}
	/**
	 * Evaluates letters with a color according to the base
	 * Wordle game.
	 */
	@Override
	protected Color[] evaluateColors() {
		Color[] backgroundColors = new Color[guess.length];
		boolean[] currentWordUsed = new boolean[currentWord.length];
		for (int column = 0; column < guess.length; column++) {
			if(guess[column] == currentWord[column]) {
				backgroundColors[column] = AppColors.GREEN;
				currentWordUsed[column] = true;
			}
			else {
				backgroundColors[column] = AppColors.GRAY;
			}
		}
		
		for (int column = 0; column < guess.length; column++) {
			if (backgroundColors[column] == AppColors.GREEN) {
				continue;				
			}
			for (int index = 0; index < currentWord.length; index++) {
				if (!currentWordUsed[index] && guess[column] == currentWord[index]) {
					backgroundColors[column] = AppColors.YELLOW;
					currentWordUsed[index] = true;
					break;
				}
			}
		}
		return backgroundColors;
	}
			
}