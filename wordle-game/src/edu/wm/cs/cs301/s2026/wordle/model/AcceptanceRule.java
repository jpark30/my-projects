package edu.wm.cs.cs301.s2026.wordle.model;

public interface AcceptanceRule {
	public boolean isAcceptableGuess(Model model);
}
