package edu.wm.cs.cs301.s2026.wordle.model;


public class RuleHard implements AcceptanceRule {
	private final AcceptanceRule nextRule;
	
	public RuleHard(AcceptanceRule nextRule) {
		this.nextRule = nextRule;
	}
	@Override
	public boolean isAcceptableGuess(Model model) {
		if (!nextRule.isAcceptableGuess(model)) {
			return false;
		}
		
		int previousRowNumber = model.getCurrentRowNumber();
	    if (previousRowNumber < 0) {
	        return true;
	    }
	    
	    WordleResponse[] previousRow = model.getWordleGrid()[previousRowNumber];
	    int currentRowNumber = previousRowNumber + 1;
	    WordleResponse[] currentRowCells= model.getWordleGrid()[currentRowNumber];
	    
	    for (int col = 0; col < model.getColumnCount(); col++) {
	    	if (previousRow[col].getBackgroundColor().equals(AppColors.GREEN)) {
	    		char requiredChar = previousRow[col].getChar();
	    		if (currentRowCells[col] == null || currentRowCells[col].getChar() != requiredChar) {
	    			System.out.println("GREEN check failed at col " + col 
	                        + " required: " + requiredChar);
	    			return false;
	    		}
	    	}
	    }
	    for (int col = 0; col < model.getColumnCount(); col++) {
	    	if (previousRow[col].getBackgroundColor().equals(AppColors.YELLOW)) {
	    		char requiredChar = previousRow[col].getChar();
	    		boolean found = false;
	    		for (int i = 0; i < model.getColumnCount(); i++) {
	    			if (currentRowCells[i] != null &&
	    				currentRowCells[i].getChar() == requiredChar){
	    					found = true;
	    					break;
	    				}
	    		}
	    		if (!found) {
	    			System.out.println("YELLOW check failed - missing: " + requiredChar);
    				return false;
    			}
	    	}
	    }
	    for (int col = 0; col < model.getColumnCount(); col++) {
	        if (previousRow[col].getBackgroundColor().equals(AppColors.GRAY)) {
	            char forbiddenChar = previousRow[col].getChar();

	            boolean isActuallyPresent = false;
	            for (int j = 0; j < model.getColumnCount(); j++) {
	                if (!previousRow[j].getBackgroundColor().equals(AppColors.GRAY) &&
	                    previousRow[j].getChar() == forbiddenChar) {
	                    isActuallyPresent = true;
	                    break;
	                }
	            }


	            if (!isActuallyPresent) {
	                for (int i = 0; i < model.getColumnCount(); i++) {
	                    if (currentRowCells[i] != null &&
	                            currentRowCells[i].getChar() == forbiddenChar) {
	                        System.out.println("GRAY check failed - forbidden: " 
	                                + forbiddenChar + " found at col " + i);
	                        return false;
	                    }
	                }
	            }
	            
	        }
	    }
	    
	    return true;
	}
}
