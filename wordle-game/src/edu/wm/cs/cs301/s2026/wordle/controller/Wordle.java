package edu.wm.cs.cs301.s2026.wordle.controller;

import javax.swing.SwingUtilities;

import edu.wm.cs.cs301.s2026.wordle.model.Model;
import edu.wm.cs.cs301.s2026.wordle.model.AbsurdleModel;
import edu.wm.cs.cs301.s2026.wordle.model.WordleModel;
import edu.wm.cs.cs301.s2026.wordle.view.WordleFrame;
import edu.wm.cs.cs301.s2026.wordle.model.AcceptanceRule;
import edu.wm.cs.cs301.s2026.wordle.model.RuleBasic;
import edu.wm.cs.cs301.s2026.wordle.model.RuleLegitimateWordsOnly;
import edu.wm.cs.cs301.s2026.wordle.model.RuleHard;
public class Wordle implements Runnable {
	
	private final Model model;
	
	private final AcceptanceRule rule;
	
	public Wordle() {
		this.model = new WordleModel();
		this.rule = new RuleBasic();
	}
	
	public Wordle(Model model, AcceptanceRule rule) {
		this.model = model;
		this.rule = rule;
	}
	
	
	public static void main(String[] args) {
		String strategy = "random";
		boolean hardMode = false;
		boolean legitimateWordsOnly = false;
		
		for (int i = 0; i <args.length; i++) {
			switch(args[i]) {
			case "-s":
				if (i + 1 < args.length){
					strategy = args[i +1].toLowerCase();
					i++;
				}
				break;
			case  "-h":
				hardMode = true;
				break;
			case "-wo":
				legitimateWordsOnly = true;
				break;
			default:
				System.out.println("Unknown argument: " + args[i]);
	            break;
			}
		}
		System.out.println("Strategy: " + strategy);
	    System.out.println("Hard mode: " + hardMode);
	    System.out.println("Words only: " + legitimateWordsOnly);
		Model model;
		if (strategy.equals("absurdle")) {
			model = new AbsurdleModel();
			
		}
		else {
			model = new WordleModel();
		}
		
		AcceptanceRule rule = new RuleBasic();
		if (legitimateWordsOnly) {
			rule = new RuleLegitimateWordsOnly(rule);
		}
		if (hardMode) {
			rule = new RuleHard(rule);
		}
		SwingUtilities.invokeLater(new Wordle(model, rule));
	}

	@Override
	public void run() {
		new WordleFrame(model,rule);
	}

}
