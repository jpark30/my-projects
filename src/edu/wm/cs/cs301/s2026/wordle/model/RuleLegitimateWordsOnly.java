package edu.wm.cs.cs301.s2026.wordle.model;

public class RuleLegitimateWordsOnly implements AcceptanceRule {
	
	private final AcceptanceRule nextRule;
	
	public RuleLegitimateWordsOnly(AcceptanceRule nextRule) {
		this.nextRule = nextRule;
	}
	@Override
	public boolean isAcceptableGuess(Model model) {
		if (!nextRule.isAcceptableGuess(model)) {
			return false;
		}
		return model.isValidGuess();
	}
}
