package edu.wm.cs.cs301.s2026.wordle.model;

public class RuleBasic implements AcceptanceRule {
	
	@Override
	public boolean isAcceptableGuess(Model model) {
		if (model.getCurrentColumn() == 4) {
			return true;
		}
		else{
			return false;
		}
	}
}
