package edu.wm.cs.cs301.s2026.wordle.controller;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;

import edu.wm.cs.cs301.s2026.wordle.model.Model;
import edu.wm.cs.cs301.s2026.wordle.model.WordleResponse;
import edu.wm.cs.cs301.s2026.wordle.view.StatisticsDialog;
import edu.wm.cs.cs301.s2026.wordle.view.WordleFrame;
import edu.wm.cs.cs301.s2026.wordle.model.AcceptanceRule;

public class KeyboardButtonAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	
	private final WordleFrame view;
	
	private final Model model;
	
	private final AcceptanceRule rule;

	public KeyboardButtonAction(WordleFrame view, Model model, AcceptanceRule rule) {
		this.view = view;
		this.model = model;
		this.rule = rule;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = (JButton) event.getSource();
		String text = button.getActionCommand();
		switch (text) {
		case "Enter":
			handleEnterKey();
			break;
		case "Backspace":
			model.backspace();
			view.repaintWordleGridPanel();
			break;
		default:
			model.setCurrentColumn(text.charAt(0));
			view.repaintWordleGridPanel();
			break;
		}
		
	}
	
	private void handleEnterKey() {
		if (!rule.isAcceptableGuess(model)) {
			clearCurrentRow();
			return;
		}
		
		if (model.getCurrentColumn() >= (model.getColumnCount() - 1)) {
			boolean moreRows = model.setCurrentRow();

			int greenCount = countGreenResponses(model.getCurrentRow());
			view.updateKeyboardPanel();
			if (greenCount >= model.getColumnCount()) {
				handleWin();
			} else if (!moreRows) {
				handleLoss();
			} else {
				view.repaintWordleGridPanel();
			}
		}
	}
	
	private int countGreenResponses(WordleResponse[] currentRow) {
		int greenCount = 0;
		for (WordleResponse wordleResponse : currentRow) {
			view.setColor(Character.toString(wordleResponse.getChar()),
					wordleResponse.getBackgroundColor(), 
					wordleResponse.getForegroundColor());
			if (wordleResponse.isGreen()) {
				greenCount++;
			} 
		}
		return greenCount;
	}
	private void handleWin() {
		view.repaintWordleGridPanel();
		model.incrementTotalGamesPlayed();
		model.addWordsGuessed(model.getCurrentRowNumber());
		model.incrementCurrentStreak();
		new StatisticsDialog(view, model);
	}
	private void handleLoss() {
		view.repaintWordleGridPanel();
		model.incrementTotalGamesPlayed();
		model.setCurrentStreak(0);
		new StatisticsDialog(view, model);
	}
	
	private void clearCurrentRow() {
		for (int i = 0; i < model.getColumnCount(); i++) {
			model.backspace();
		}
		view.repaintWordleGridPanel();
	}
}
