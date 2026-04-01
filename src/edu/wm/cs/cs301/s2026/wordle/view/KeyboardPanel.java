package edu.wm.cs.cs301.s2026.wordle.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import edu.wm.cs.cs301.s2026.wordle.controller.KeyboardButtonAction;
import edu.wm.cs.cs301.s2026.wordle.model.AppColors;
import edu.wm.cs.cs301.s2026.wordle.model.Model;
import edu.wm.cs.cs301.s2026.wordle.model.AcceptanceRule;


public class KeyboardPanel {

	private int buttonIndex, buttonCount;

	private final JButton[] buttons;

	private final JPanel panel;

	private final KeyboardButtonAction action;

	private final Model model;
	
	private JLabel remainingWordsLabel;

	public KeyboardPanel(WordleFrame view, Model model, AcceptanceRule rule) {
		this.model = model;
		buttonIndex = 0;
		buttonCount = firstRow().length + secondRow().length
				+ thirdRow().length;
		buttons = new JButton[buttonCount];
		action = new KeyboardButtonAction(view, model, rule);
		panel = createMainPanel();
	}

	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new GridLayout(0, 1, 0, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 5));

		panel.add(createQPanel());
		panel.add(createAPanel());
		panel.add(createZPanel());
		panel.add(createTotalPanel());

		return panel;
	}

	private JPanel createQPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		Font textfont = AppFonts.getTextFont();

		String[] letters = firstRow();

		for (int index = 0; index < letters.length; index++) {
			JButton button = new JButton(letters[index]);
			setKeyBinding(button, letters[index]);
			button.addActionListener(action);
			button.setFont(textfont);
			buttons[buttonIndex++] = button;
			panel.add(button);
		}

		return panel;
	}

	private String[] firstRow() {
		String[] letters = { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P",
				"Backspace" };
		return letters;
	}

	private JPanel createAPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		Font textfont = AppFonts.getTextFont();

		String[] letters = secondRow();

		for (int index = 0; index < letters.length; index++) {
			JButton button = new JButton(letters[index]);
			setKeyBinding(button, letters[index]);
			button.addActionListener(action);
			button.setFont(textfont);
			buttons[buttonIndex++] = button;
			panel.add(button);
		}

		return panel;
	}

	private String[] secondRow() {
		String[] letters = { "A", "S", "D", "F", "G", "H", "J", "K", "L",
				"Enter" };
		return letters;
	}

	private JPanel createZPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		Font textfont = AppFonts.getTextFont();

		String[] letters = thirdRow();

		for (int index = 0; index < letters.length; index++) {
			JButton button = new JButton(letters[index]);
			setKeyBinding(button, letters[index]);
			button.addActionListener(action);
			button.setFont(textfont);
			buttons[buttonIndex++] = button;
			panel.add(button);
		}

		return panel;
	}

	private String[] thirdRow() {
		String[] letters = { "Z", "X", "C", "V", "B", "N", "M" };
		return letters;
	}

	private void setKeyBinding(JButton button, String text) {
		InputMap inputMap = button.getInputMap(JButton.WHEN_IN_FOCUSED_WINDOW);
		if (text.equalsIgnoreCase("Backspace")) {
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
					"action");
		} else {
			inputMap.put(KeyStroke.getKeyStroke(text.toUpperCase()), "action");
		}
		ActionMap actionMap = button.getActionMap();
		actionMap.put("action", action);
	}

	private JPanel createTotalPanel() {
		JPanel panel = new JPanel(new FlowLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		Font footerFont = AppFonts.getFooterFont();

		String text = String.format("%,d", model.getTotalWordCount());
		text += " possible " + model.getColumnCount() + "-letter words!";
		remainingWordsLabel = new JLabel(text);
		remainingWordsLabel.setFont(footerFont);
		panel.add(remainingWordsLabel);

		return panel;
	}

	public void setColor(String letter, Color backgroundColor,
			Color foregroundColor) {
		for (JButton button : buttons) {
			if (button.getActionCommand().equals(letter)) {
				Color color = button.getBackground();
				if (color.equals(AppColors.GREEN)) {
					// Do nothing
				} else if (color.equals(AppColors.YELLOW)
						&& backgroundColor.equals(AppColors.GREEN)) {
					button.setBackground(backgroundColor);
					button.setForeground(foregroundColor);
				} else {
					button.setBackground(backgroundColor);
					button.setForeground(foregroundColor);
				}
				break;
			}
		}
	}

	public void resetDefaultColors() {
		for (JButton button : buttons) {
			button.setBackground(null);
			button.setForeground(null);
		}
	}
	
	public void update() {
		String text = String.format("%,d", model.getRemainingWordCount());
		text += " possible" + model.getColumnCount() + "-letter words!";
		remainingWordsLabel.setText(text);
	    remainingWordsLabel.repaint();
	}

	public JPanel getPanel() {
		return panel;
	}

}
