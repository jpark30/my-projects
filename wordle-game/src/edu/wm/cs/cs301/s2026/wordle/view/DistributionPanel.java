package edu.wm.cs.cs301.s2026.wordle.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import edu.wm.cs.cs301.s2026.wordle.model.AppColors;
import edu.wm.cs.cs301.s2026.wordle.model.Model;

public class DistributionPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	
	private double[] percentages;
	
	private int[] counts;
	private int lastValue;
	
	private final Model model;

	public DistributionPanel(WordleFrame view, Model model) {
		this.model = model;
		calculatePercentages();
		setPreferredSize(new Dimension(500, 200));
	}
	
	private void calculatePercentages() {
		counts = model.calculateArrayOfWins();
		lastValue = model.getLastWin() - 1;
		
		int maxCount = 0;
		for (int index = 0; index < model.getMaximumRows(); index++) {
			maxCount = Math.max(maxCount, counts[index]);
		}
		
		percentages = new double[model.getMaximumRows()];
		for (int index = 0; index < model.getMaximumRows(); index++) {
			percentages[index] = (double) counts[index] / maxCount;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	
		Font textFont = AppFonts.getTextFont();
		g2d.setFont(textFont);
		FontMetrics metrics = g2d.getFontMetrics(textFont);
		
		int margin = metrics.getHeight() / 3;
		int x = 20;
		int x1 = x + 20;
		int y = 20;
		int y1 = getWidth() - 30;
		int y2 = 20;
		int difference = y1 - y - y2;

		for (int index = 0; index < model.getMaximumRows(); index++) {
			String text = Integer.toString(index + 1);
			g2d.setColor(Color.BLACK);
			g2d.drawString(text, x, y + 2);

			if (index == lastValue
					&& model.getCurrentStreak() > 0) {
				g2d.setColor(AppColors.GREEN);
			} else {
				g2d.setColor(AppColors.GRAY);
			}
			
			int pixelWidth = (int) (Math.round(percentages[index] * difference)
					+ y2);
			g2d.fillRect(x1, y - metrics.getHeight() + margin, pixelWidth,
					metrics.getHeight());
			g2d.setColor(Color.WHITE);
			text = String.format("%,d", counts[index]);
			int textWidth = metrics.stringWidth(text);
			g2d.drawString(Integer.toString(counts[index]),
					x1 + pixelWidth - textWidth - 6, y + 2);
			
			y += metrics.getHeight() + margin;
		}
		
	}

}
