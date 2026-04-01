package edu.wm.cs.cs301.s2026.wordle.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tracks and persists cumulative game statistics for the Wordle game across sessions.
 *
 * <p>This class maintains the player's current win streak, longest win streak,
 * total games played, and a history of how many guesses each won game required.
 * Statistics are automatically read from a log file on disk when this object is
 * constructed, and can be saved back to disk by calling {@link #writeStatistics()}.
 * The log file is stored in a "Wordle" folder inside the user's home directory.
 * If no log file exists yet (first time playing), all counters start at zero.</p>
 */
public class Statistics {

	/**
	 * The player's current consecutive win streak.
	 * Resets to 0 on a loss. Range is 0 or greater.
	 * Initialized by {@link #readStatistics()} from disk, or 0 if no file exists.
	 */
	private int currentStreak;

	/**
	 * The player's longest consecutive win streak across all sessions.
	 * Never decreases — updated automatically in {@link #setCurrentStreak(int)}
	 * whenever {@code currentStreak} exceeds it.
	 * Initialized by {@link #readStatistics()} from disk, or 0 if no file exists.
	 */
	private int longestStreak;

	/**
	 * The total number of games played, regardless of win or loss.
	 * Incremented by {@link #incrementTotalGamesPlayed()} at the end of each game.
	 * Initialized by {@link #readStatistics()} from disk, or 0 if no file exists.
	 */
	private int totalGamesPlayed;

	/**
	 * A list recording how many guesses each won game required.
	 * Each entry is an integer between 1 and 6 (the number of rows used to win).
	 * Lost games are not recorded here. Entries are added via {@link #addWordsGuessed(int)}.
	 * Initialized as an empty list in the constructor, then populated from disk
	 * by {@link #readStatistics()}.
	 */
	private final List<Integer> wordsGuessed;

	/**
	 * The file system path to the directory where the statistics log is stored.
	 * Resolves to the "Wordle" folder inside the user's home directory.
	 * Set once in the constructor using system properties and never changed (final).
	 */
	private final String path;

	/**
	 * The file name (including the path separator prefix) for the statistics log file.
	 * Combined with {@link #path} to form the full file path, e.g. "/statistics.log".
	 * Set once in the constructor and never changed (final).
	 */
	private final String log;

	/**
	 * No-parameter constructor that initializes all fields and loads existing statistics from disk.
	 *
	 * <p>Sets up the file path using the user's home directory and the platform-specific
	 * file separator. Initializes {@link #wordsGuessed} as an empty list, then calls
	 * {@link #readStatistics()} to populate fields from the log file if it exists.
	 * If the file does not exist, all counters remain at 0.</p>
	 */
	public Statistics() {
		wordsGuessed = new ArrayList<>();

		// Use the OS-specific file separator (e.g. "/" on Linux, "\" on Windows)
		final String fileSeparator = System.getProperty("file.separator");

		// Build the path to ~/Wordle/statistics.log
		path = System.getProperty("user.home") + fileSeparator + "Wordle";
		log = fileSeparator + "statistics.log";

		readStatistics();
	}

	/**
	 * Reads previously saved statistics from the log file on disk and populates all fields.
	 *
	 * <p>Opens the log file at {@code path + log} and reads each value line by line
	 * in this order: currentStreak, longestStreak, totalGamesPlayed, total number of
	 * wordsGuessed entries, then one integer per line for each entry in wordsGuessed.
	 * If the file does not exist ({@link FileNotFoundException}), all counters are
	 * set to 0 silently — this is expected on first run. Any other IO error is
	 * printed to the console.</p>
	 */
	private void readStatistics() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(path + log));

			// Read each statistic line by line in the order they were written
			currentStreak = Integer.valueOf(br.readLine().trim());
			longestStreak = Integer.valueOf(br.readLine().trim());
			totalGamesPlayed = Integer.valueOf(br.readLine().trim());

			// Read how many guess-count entries to expect, then read each one
			final int totalWordsGuessed = Integer.valueOf(br.readLine().trim());
			for (int index = 0; index < totalWordsGuessed; index++) {
				wordsGuessed.add(Integer.valueOf(br.readLine().trim()));
			}

			br.close();
		} catch (FileNotFoundException e) {
			// No log file found — this is normal on the first run, start fresh
			currentStreak = 0;
			longestStreak = 0;
			totalGamesPlayed = 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the current statistics to the log file on disk, creating it if necessary.
	 *
	 * <p>Creates the "Wordle" directory and log file if they do not already exist.
	 * Writes each statistic on its own line in this order: currentStreak, longestStreak,
	 * totalGamesPlayed, the count of wordsGuessed entries, then one entry per line.
	 * This method should be called at the end of each game to persist the updated state.
	 * Any IO errors are printed to the console.</p>
	 */
	public void writeStatistics() {
		try {
			// Create the ~/Wordle directory if it does not exist yet
			File file = new File(path);
			file.mkdir();

			// Create the statistics.log file if it does not exist yet
			file = new File(path + log);
			file.createNewFile();

			final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

			// Write each field on its own line, in the same order readStatistics() expects
			bw.write(Integer.toString(currentStreak));
			bw.write(System.lineSeparator());
			bw.write(Integer.toString(longestStreak));
			bw.write(System.lineSeparator());
			bw.write(Integer.toString(totalGamesPlayed));
			bw.write(System.lineSeparator());

			// Write the number of guess-count entries first, then each entry
			bw.write(Integer.toString(wordsGuessed.size()));
			bw.write(System.lineSeparator());
			for (Integer value : wordsGuessed) {
				bw.write(Integer.toString(value));
				bw.write(System.lineSeparator());
			}

			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the player's current consecutive win streak.
	 *
	 * @return the current streak as a non-negative integer
	 */
	public int getCurrentStreak() {
		return currentStreak;
	}

	/**
	 * Sets the current win streak and updates the longest streak if the new value exceeds it.
	 *
	 * <p>This method should be called after each game — pass the incremented streak value
	 * on a win, or 0 on a loss. The longest streak is automatically updated here so it
	 * never needs to be set separately.</p>
	 *
	 * @param currentStreak the new current streak value; should be 0 or greater
	 */
	public void setCurrentStreak(int currentStreak) {
		this.currentStreak = currentStreak;

		// Automatically update longestStreak if the new streak surpasses it
		if (currentStreak > longestStreak) {
			longestStreak = currentStreak;
		}
	}

	/**
	 * Returns the player's longest consecutive win streak across all sessions.
	 *
	 * @return the longest streak as a non-negative integer
	 */
	public int getLongestStreak() {
		return longestStreak;
	}

	/**
	 * Returns the total number of games played, including both wins and losses.
	 *
	 * @return total games played as a non-negative integer
	 */
	public int getTotalGamesPlayed() {
		return totalGamesPlayed;
	}

	/**
	 * Increments the total games played counter by one.
	 *
	 * <p>Should be called exactly once at the end of every game, regardless of
	 * whether the player won or lost.</p>
	 */
	public void incrementTotalGamesPlayed() {
		totalGamesPlayed++;
	}

	/**
	 * Returns the list recording how many guesses each won game required.
	 *
	 * <p>Each entry is an integer between 1 and 6 representing the row on which
	 * the player correctly guessed the word. Lost games are not included.
	 * The returned list is the live internal list — callers should not modify it
	 * directly; use {@link #addWordsGuessed(int)} instead.</p>
	 *
	 * @return the list of guess counts for won games
	 */
	public List<Integer> getWordsGuessed() {
		return wordsGuessed;
	}

	/**
	 * Records the number of guesses used in a won game.
	 *
	 * <p>Should be called only when the player wins. The value passed should be
	 * the row number (1 through 6) on which the correct guess was made.</p>
	 *
	 * @param wordCount the number of guesses used to win, expected to be between 1 and 6
	 */
	public void addWordsGuessed(int wordCount) {
		wordsGuessed.add(wordCount);
	}
	
	public int getTotalGamesWon() {
    	return wordsGuessed.size();
    }
    public int getLastWin() {
        if (wordsGuessed.isEmpty()) {
            return 0;
        }
        return wordsGuessed.get(wordsGuessed.size() - 1);
    }
    public int[] calculateArrayOfWins(int maximumTries) {
        int[] wins = new int[maximumTries];
        for (int value : wordsGuessed) {
            if (value >= 1 && value <= maximumTries) {
                wins[value-1]++;
            }
        }
        return wins;
    }

}
