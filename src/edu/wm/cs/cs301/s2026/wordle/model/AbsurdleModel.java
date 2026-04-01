package edu.wm.cs.cs301.s2026.wordle.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.io.IOException;
/**
 * This class implements the Absurdle game logic and game state
 */
public class AbsurdleModel extends AbstractModel {
	/**
	 * Necessary field to contain all possible guesses.
	 */
	private List<String> remainingWords;
	
	private static final Logger LOGGER =
			Logger.getLogger(AbsurdleModel.class.getName());

	
	/**
	 * No parameter constructor, calls AbstractModel constructor
	 */
	public AbsurdleModel() {
		super();
		LOGGER.setLevel(Level.INFO);
	    try {
	        FileHandler fileTxt = new FileHandler("./logging.txt", true);
	        // true means append to existing file rather than overwrite
	        LOGGER.addHandler(fileTxt);
	    } catch (SecurityException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	/**
	 * Overrides initializer from AbstractModel.
	 * Remaining guesses are put in remainingWords,
	 * currentWord is set to null.
	 */
	@Override
	public void initialize() {
		super.initialize();
		
		try {
	        wordListLatch.await();
	    } catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        System.err.println("Interrupted while waiting for word list: " + e.getMessage());
	    }
		remainingWords = new ArrayList<>(wordList);
		currentWord = null;
		generateCurrentWord();
		LOGGER.info("Absurdle initialized with " + remainingWords.size() + " possible words");
	}
	/**
	 * Handles the case where there is only one possible word remaining.
	 */
	@Override
	public void generateCurrentWord() {
		if (remainingWords != null && remainingWords.size() == 1) {
            currentWord = remainingWords.get(0).toUpperCase().toCharArray();
        }
	}
	/**
	 * Returns number of possible words remaining in the wordlist
	 */
	@Override
	public int getRemainingWordCount() {
		if (remainingWords == null) return 0;
	    return remainingWords.size();
	}
	/**
	 * Evaluates letter colors according to the Absurdle game.
	 * @return backgroundColors, an array containing each letters' color.
	 */
	@Override
	protected Color[] evaluateColors() {
		Map<String, List<String>> patternGroups = new HashMap<>();
		for (String word : remainingWords) {
			Color[] pattern = calculatePattern(guess, word.toUpperCase().toCharArray());
			String patternKey = patternToString(pattern);
			if (!patternGroups.containsKey(patternKey)) {
				patternGroups.put(patternKey, new ArrayList<>());
			}
			patternGroups.get(patternKey).add(word);
		}
		
		String chosenPatternKey = null;
		List<String> largestGroup = null;
	
		for (Map.Entry<String, List<String>> entry : patternGroups.entrySet()) {
			if (largestGroup == null || entry.getValue().size() > largestGroup.size()) {
				largestGroup = entry.getValue();
				chosenPatternKey = entry.getKey();
			}
		}
		remainingWords = largestGroup;
		generateCurrentWord();
		
		// Log in the same format as the reference implementation
		// "response [pattern] would leave [count] words"
		String logMessage = "response " + chosenPatternKey + 
		        " would leave " + remainingWords.size() + " words";

		// If 10 or fewer words remain, list them
		if (remainingWords.size() <= 10) {
		    logMessage += ": " + remainingWords;
		}

		LOGGER.info(logMessage);
		return stringToPattern(chosenPatternKey);
	}
	/**
	 * Calculates the color pattern of the user guess.
	 * @param guess the current guess stored as char array
	 * @param targetWord the target word stored as char array
	 * @return array of colors representing the pattern for each column.
	 */
	private Color[] calculatePattern(char[] guess, char[] targetWord) {
		Color[] pattern = new Color[guess.length];
		boolean[] targetUsed = new boolean[targetWord.length];
		for (int col = 0; col < guess.length; col++) {
			if (guess[col] == targetWord[col]) {
				pattern[col] = AppColors.GREEN;
				targetUsed[col] = true;
			}
			else {
				pattern[col] = AppColors.GRAY;
			}
		}
		for (int col = 0; col < guess.length; col++) {
			if(pattern[col] == AppColors.GREEN) {
				continue;
			}
			for (int idx = 0; idx < targetWord.length; idx++) {
				if (!targetUsed[idx] && guess[col] == targetWord[idx]) {
					pattern[col] = AppColors.YELLOW;
					targetUsed[idx] = true;
					break;
				}
			}
		}
		return pattern;
	}
	/**
	 * Calculates the string for a pattern of colors Green,
	 * Gray, and Yellow. They are respectively labeled in the 
	 * string as G, R, Y
	 * @param pattern array of colors representing the pattern for each column
	 * @return string representing the color pattern
	 */
	private String patternToString(Color[] pattern) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < pattern.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			if (pattern[i].equals(AppColors.GREEN)) {
				sb.append("G");
			}
			else if (pattern[i].equals(AppColors.YELLOW)) {
				sb.append("Y");
			}
			else {
				sb.append("R");
			}
		}
		return sb.toString();
	}
	/**
	 * Calculates the array of colors given a string from
	 * patternToString. The reverse of patternToString.
	 * @param patternKey string representing the color pattern
	 * @return array of colors representing the patternKey string
	 */
	private Color[] stringToPattern(String patternKey) {
		String[] parts = patternKey.split(" ");
		Color[] pattern = new Color[parts.length];
		for (int i = 0; i < parts.length; i++) {
			switch (parts[i]) {
			case "G": pattern[i] = AppColors.GREEN; break;
			case "Y": pattern[i] = AppColors.YELLOW; break;
			default: pattern[i] = AppColors.GRAY; break;
			}
		}
		return pattern;
	}
	
}
