package edu.wm.cs.cs301.s2026.wordle.model;

import java.awt.Color;

/**
 * Defines the standard color constants used throughout the Wordle game UI.
 *
 * <p>This utility class holds static final {@link Color} constants representing
 * the four colors used to render the game grid and its outlines. Rather than
 * scattering raw RGB values across the codebase, all colors are defined here
 * in one place so they can be reused consistently and changed easily.
 * This class is not meant to be instantiated — it exists purely as a
 * container for shared color constants.</p>
 */
public class AppColors {

	/**
	 * Gray color used as the background for letters that are not present in the target word.
	 * RGB value: (120, 124, 126).
	 * Applied by {@code WordleModel.setCurrentRow()} when no match is found.
	 */
	public static final Color GRAY = new Color(120, 124, 126);

	/**
	 * Green color used as the background for letters that are in the correct position.
	 * RGB value: (106, 170, 100).
	 * Applied by {@code WordleModel.setCurrentRow()} when a letter matches exactly.
	 */
	public static final Color GREEN = new Color(106, 170, 100);

	/**
	 * Yellow color used as the background for letters that are in the target word
	 * but in the wrong position.
	 * RGB value: (201, 180, 88).
	 * Applied by {@code WordleModel.setCurrentRow()} when a letter exists elsewhere
	 * in the target word.
	 */
	public static final Color YELLOW = new Color(201, 180, 88);

	/**
	 * Light gray color used to draw the outline or border of unfilled grid cells.
	 * RGB value: (211, 214, 218).
	 * Used by the view layer to render empty or inactive cells.
	 */
	public static final Color OUTLINE = new Color(211, 214, 218);

}