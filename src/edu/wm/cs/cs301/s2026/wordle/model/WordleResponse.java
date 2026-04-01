package edu.wm.cs.cs301.s2026.wordle.model;

import java.awt.Color;

/**
 * Represents a single cell in the Wordle grid, storing a letter and its display colors.
 *
 * <p>This immutable class is used by {@code WordleModel} to populate the game grid.
 * Each instance holds one guessed character along with a background color
 * (indicating correctness: green, yellow, or gray) and a foreground color
 * (the text color used to display the character). All fields are set once
 * at construction and cannot be changed afterward.</p>
 */
public class WordleResponse {

	/**
	 * The guessed character for this cell.
	 * Must be an uppercase letter A-Z as enforced by {@code WordleModel}.
	 * Set once in the constructor and never changed (final).
	 */
	private final char c;

	/**
	 * The background color of this cell, indicating the correctness of the guess.
	 * Expected values are {@link AppColors#GREEN} (correct position),
	 * {@link AppColors#YELLOW} (wrong position), {@link AppColors#GRAY} (not in word),
	 * or {@link Color#WHITE} (letter typed but not yet submitted).
	 * Set once in the constructor and never changed (final).
	 */
	private final Color backgroundColor;

	/**
	 * The foreground (text) color used to display the character in this cell.
	 * Typically {@link Color#BLACK} for unsubmitted letters and {@link Color#WHITE}
	 * after a guess is evaluated.
	 * Set once in the constructor and never changed (final).
	 */
	private final Color foregroundColor;

	/**
	 * Constructs a new {@code WordleResponse} with the given character and display colors.
	 *
	 * <p>All three values are required and stored as-is. No validation is performed
	 * on the character or colors — the caller is responsible for passing correct values.</p>
	 *
	 * @param c               the guessed character to display in this cell, expected to be uppercase A-Z
	 * @param backgroundColor the background color indicating correctness of the guess
	 * @param foregroundColor the text color used to render the character
	 */
	public WordleResponse(char c, Color backgroundColor, Color foregroundColor) {
		this.c = c;
		this.backgroundColor = backgroundColor;
		this.foregroundColor = foregroundColor;
	}

	/**
	 * Returns the character stored in this cell.
	 *
	 * @return the guessed character, expected to be an uppercase letter A-Z
	 */
	public char getChar() {
		return c;
	}

	/**
	 * Returns the background color of this cell.
	 *
	 * <p>The background color reflects the correctness of the guess:
	 * green for the correct position, yellow for wrong position,
	 * gray for not in the word, or white if the letter has been typed
	 * but the row has not yet been submitted.</p>
	 *
	 * @return the background {@link Color} of this cell
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Returns the foreground (text) color of this cell.
	 *
	 * <p>Used by the view layer to render the character on screen
	 * with appropriate contrast against the background color.</p>
	 *
	 * @return the foreground {@link Color} used to display the character
	 */
	public Color getForegroundColor() {
		return foregroundColor;
	}
	
	public boolean isGreen() {
		return this.backgroundColor.equals(AppColors.GREEN);
	}

}

