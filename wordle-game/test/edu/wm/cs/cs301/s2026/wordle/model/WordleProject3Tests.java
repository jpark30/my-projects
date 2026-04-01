package edu.wm.cs.cs301.s2026.wordle.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WordleProject3Tests {
	private Model model;
	//HELPER FUNCTIONS AND BEFOREEACH
	@BeforeEach
	public void setUp() throws InterruptedException {
	    model = new WordleModel();
	    while (model.getTotalWordCount() == 0) {
	        Thread.sleep(10); // wait for background thread to finish
	    }
	    List<String> words = new ArrayList<>();
	    words.add("slate");
	    model.setWordList(words);
	}

	private void typeWords(String word) {
		for (char c : word.toUpperCase().toCharArray()) {
			model.setCurrentColumn(c);
		}
	}

	private void setupWithWordList(List<String> words) throws InterruptedException {
	    while (model.getTotalWordCount() == 0) {
	        Thread.sleep(10); // wait for background thread to finish
	    }
	    model.setWordList(words);
	    model.initialize();
	}
	/*
	 * Test background thread finishes intializing wordlist given enough time.
	 */
	@Test
	public void testBug1ThreadSynchronization() throws InterruptedException {
		
		Thread.sleep(100);	
		assertNotNull(model.getCurrentWord());
		
		
	}
	/* 
	 * Make sure await function work by calling wordlist immediately.
	 */
	@Test
	public void testBug1_2ThreadSynchronization() throws InterruptedException {

	    assertNotNull(model.getCurrentWord());
	}
	
	@Test
	public void testBug2Coloring() throws InterruptedException {
	    List<String> words = new ArrayList<>();
	    words.add("blurb");
	    words.add("hello");
	    setupWithWordList(words);
	    model.setCurrentWord("blurb");
	    typeWords("hello");
	    model.setCurrentRow();

	    WordleResponse[] row = model.getCurrentRow();

	    // h - gray, not in "blurb"
	    assertEquals(AppColors.GRAY, row[0].getBackgroundColor());
	    // e - gray, not in "blurb"
	    assertEquals(AppColors.GRAY, row[1].getBackgroundColor());
	    // first l - yellow, 'l' exists once in "blurb"
	    assertEquals(AppColors.YELLOW, row[2].getBackgroundColor());
	    // second l - gray, 'l' already claimed
	    assertEquals(AppColors.GRAY, row[3].getBackgroundColor());
	    // o - gray, not in "blurb"
	    assertEquals(AppColors.GRAY, row[4].getBackgroundColor());
	}
	
	@Test
	public void testBug3MustGuessRealWords() throws InterruptedException{
		List<String> words = new ArrayList<>();
	    words.add("slate");
	    
	    setupWithWordList(words);

	    // type an invalid word
	    typeWords("abcde");
	    model.setCurrentRow();

	    // row should not have advanced since "abcde" is not in the word list
	    assertNull(model.getWordleGrid()[1][0]); // row 1 should still be empty
	}
	
	@Test
	public void testBug4Backspace() throws InterruptedException {
	    List<String> words = new ArrayList<>();
	    words.add("slate");
	    words.add("blurb");
	    setupWithWordList(words);

	    // type one letter then backspace
	    typeWords("s");
	    model.backspace();

	    // first cell should be null since it was backspaced
	    assertNull(model.getWordleGrid()[0][0]);

	    // type a full word after backspacing from first position
	    typeWords("blurb");
	    model.setCurrentRow();

	    // row should have advanced meaning the word was accepted
	    assertNotNull(model.getWordleGrid()[0][0]);	
	}

	

}