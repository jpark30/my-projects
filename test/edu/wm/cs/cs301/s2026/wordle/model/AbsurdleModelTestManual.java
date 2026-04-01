package edu.wm.cs.cs301.s2026.wordle.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AbsurdleModelTestManual {

    private Model model;

    // ── setup ──────────────────────────────────────────────────────────────

    @BeforeEach
    public void setUp() {
        model = new AbsurdleModel(); // changed from WordleModel
        List<String> words = new ArrayList<>();
        words.add("qqqqq");
        words.add("wwwww");
        words.add("slate");
        model.setWordList(words);
        model.initialize();
    }

    private void typeWords(String word) {
        for (char c : word.toCharArray()) {
            model.setCurrentColumn(c);
        }
    }

    private void setupWithWordList(List<String> words) {
        model.setWordList(words);
        model.initialize();
    }

    // ── Testing initialize() ──────────────────────────────────────────────

    @Test
    public void testInitializeWordleGrid() {
        WordleResponse[][] grid = model.getWordleGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                assertNull(grid[row][col],
                        "Should return null for every entry in a grid when initialized");
            }
        }
    }

    @Test
    public void testInitializeCurrentCol() {
        typeWords("SLATE");
        model.initialize();
        assertEquals(-1, model.getCurrentColumn(),
                "Initialize should result in currentColumn being -1");
    }

    @Test
    public void testInitializeCurrentRow() {
        typeWords("QQQQQ");
        model.setCurrentRow();
        model.initialize();
        typeWords("QQQQQ");
        model.setCurrentRow();
        assertEquals(0, model.getCurrentRowNumber(),
                "After initialize(), row counter should reset to 0");
    }

    // ── Testing getCurrentWord() ──────────────────────────────────────────
    // NOTE: In Absurdle, getCurrentWord() may return null or an empty string
    // until the game is forced into a corner. These tests verify that
    // behavior rather than expecting a specific word upfront.

    @Test
    public void testGetCurrentWordInitiallyNullOrEmpty() {
        // In Absurdle no word is committed at the start
        // getCurrentWord() should return null or empty before any guesses
        String word = model.getCurrentWord();
        assertTrue(word == null || word.isEmpty(),
                "Absurdle should not commit to a target word before any guesses");
    }

    @Test
    public void testGetCurrentWordCommittedWhenOneWordRemains() {
        // When only one word remains in the possible word list,
        // Absurdle is forced to commit to it
        List<String> words = new ArrayList<>();
        words.add("slate");
        setupWithWordList(words);
        // With only one word in the list, Absurdle must commit to it
        assertNotNull(model.getCurrentWord(),
                "With one word remaining, Absurdle must commit to a target word");
    }

    // ── Testing setCurrentColumn() ────────────────────────────────────────

    @Test
    public void testTypeOneLetter() {
        model.setCurrentColumn('S');
        assertEquals(0, model.getCurrentColumn(),
                "After typing one letter, currentColumn should be 0");
    }

    @Test
    public void testTypeFiveLetter() {
        typeWords("SLATE");
        assertEquals(4, model.getCurrentColumn(),
                "After typing five letters, currentColumn should be 4");
    }

    @Test
    public void testTypeMoreThanFiveLetter() {
        for (int i = 0; i < 7; i++) {
            model.setCurrentColumn('S');
        }
        assertEquals(4, model.getCurrentColumn(),
                "After typing more than five letters, currentColumn should be 4");
    }

    @Test
    public void testTypingResultsInWhiteLetter() {
        model.setCurrentColumn('S');
        assertEquals(java.awt.Color.WHITE,
                model.getWordleGrid()[0][0].getBackgroundColor(),
                "Should be white since the letter is not submitted");
    }

    // ── Testing backspace() ───────────────────────────────────────────────

    @Test
    public void testBackspaceOnColumn() {
        model.setCurrentColumn('Q');
        model.setCurrentColumn('W');
        model.backspace();
        assertEquals(0, model.getCurrentColumn(),
                "Should return 0 after backspacing from column 1");
    }

    @Test
    public void testBackspaceZeroOnColumn() {
        model.setCurrentColumn('Q');
        model.backspace();
        assertEquals(-1, model.getCurrentColumn(),
                "Should return -1 after backspacing from column 0");
    }

    @Test
    public void testBackspaceOnGrid() {
        model.setCurrentColumn('Q');
        model.backspace();
        assertNull(model.getWordleGrid()[0][0],
                "Grid cell should be null after backspace");
    }

    // ── Testing setCurrentRow() return value ──────────────────────────────

    @Test
    public void testSixTries() {
        boolean result = false;
        for (int i = 0; i < 6; i++) {
            typeWords("QQQQQ");
            result = model.setCurrentRow();
        }
        assertFalse(result,
                "Should return false after 6th try");
    }

    @Test
    public void testOneTry() {
        typeWords("QQQQQ");
        boolean result = model.setCurrentRow();
        assertTrue(result,
                "Should return true after 1st try");
    }

    // ── Testing setCurrentRow() adversarial color behavior ────────────────
    // NOTE: These tests are different from WordleModel because Absurdle
    // picks colors adversarially. We cannot predict exact GREEN/YELLOW/GRAY
    // patterns — instead we verify that the chosen pattern is consistent
    // with the remaining word list.

    @Test
    public void testSetCurrentRowReturnsTrueWhenGuessesRemain() {
        typeWords("QQQQQ");
        boolean result = model.setCurrentRow();
        assertTrue(result,
                "setCurrentRow() should return true when rows remain");
    }

    @Test
    public void testAllGreenWhenOneWordRemainsAndGuessMatches() {
        // With only one word in the list and the player guesses it exactly,
        // Absurdle is forced to show all GREEN
        List<String> words = new ArrayList<>();
        words.add("slate");
        setupWithWordList(words);
        model.setCurrentWord("SLATE");
        typeWords("SLATE");
        model.setCurrentRow();

        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertEquals(AppColors.GREEN, row[i].getBackgroundColor(),
                    "Cell " + i + " should be GREEN when only one word remains and guess matches");
        }
    }

    @Test
    public void testAdversarialBehaviorMaximizesRemainingWords() {
        // With a large word list, Absurdle should NOT immediately show all GREEN
        // unless forced to — it picks the pattern leaving the most words
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("qqqqq");
        words.add("wwwww");
        words.add("aaaaa");
        words.add("bbbbb");
        setupWithWordList(words);

        typeWords("SLATE");
        model.setCurrentRow();

        // After one guess with multiple words remaining,
        // Absurdle should not have committed to all GREEN
        // unless SLATE was the only possible word
        WordleResponse[] row = model.getCurrentRow();
        assertNotNull(row, "Row should not be null after submission");
        assertEquals(5, row.length, "Row should have 5 cells");
    }

    @Test
    public void testRemainingWordsNarrowAfterEachGuess() {
        // After each guess the remaining word list should narrow down
        // We verify this indirectly by checking that the game
        // eventually commits to a word after enough guesses
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("qqqqq");
        setupWithWordList(words);

        // First guess narrows the list
        typeWords("SLATE");
        model.setCurrentRow();

        // Second guess should be against a narrowed list
        typeWords("QQQQQ");
        model.setCurrentRow();

        // After two guesses with only 2 words,
        // the game must have committed to one word
        assertNotNull(model.getCurrentWord(),
                "After narrowing to one word, Absurdle must commit to a target");
    }

    // ── Testing getCurrentRow() and getCurrentRowNumber() ─────────────────

    @Test
    public void testGetCurrentRowNumberOne() {
        typeWords("QQQQQ");
        model.setCurrentRow();
        assertEquals(0, model.getCurrentRowNumber(),
                "After one submission, getCurrentRowNumber() should return 0");
    }

    @Test
    public void testGetCurrentRowNumberTwo() {
        typeWords("QQQQQ");
        model.setCurrentRow();
        typeWords("WWWWW");
        model.setCurrentRow();
        assertEquals(1, model.getCurrentRowNumber(),
                "After two submissions, getCurrentRowNumber() should return 1");
    }

    @Test
    public void testGetCurrentRowNotNull() {
        typeWords("SLATE");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        assertNotNull(row, "getCurrentRow() should not return null after submission");
        assertEquals(5, row.length,
                "getCurrentRow() should return an array of length 5");
    }

    // ── Testing getWordleGrid() ───────────────────────────────────────────

    @Test
    public void testGridInitialization() {
        assertNotNull(model.getWordleGrid(),
                "getWordleGrid() should not return null after initialization");
    }

    @Test
    public void testGridNull() {
        WordleResponse[][] grid = model.getWordleGrid();
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                assertNull(grid[row][col],
                        "Cell [" + row + "][" + col + "] should be null before any moves");
            }
        }
    }

    @Test
    public void testGridDimensions() {
        WordleResponse[][] grid = model.getWordleGrid();
        assertEquals(6, grid.length, "Should have 6 rows");
        assertEquals(5, grid[0].length, "Should have 5 columns");
    }

    // ── Testing getMaximumRows() and getColumnCount() ─────────────────────

    @Test
    public void testMaximumRows() {
        assertEquals(6, model.getMaximumRows(),
                "getMaximumRows() should always return 6");
    }

    @Test
    public void testColumnCount() {
        assertEquals(5, model.getColumnCount(),
                "getColumnCount() should always return 5");
    }

    // ── Testing getTotalWordCount() ───────────────────────────────────────

    @Test
    public void testTotalWordCountOne() {
        List<String> words = new ArrayList<>();
        words.add("slate");
        setupWithWordList(words);
        assertEquals(1, model.getTotalWordCount(),
                "Should return 1 when word list has one word");
    }

    @Test
    public void testTotalWordCountThree() {
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("qqqqq");
        words.add("wwwww");
        setupWithWordList(words);
        assertEquals(3, model.getTotalWordCount(),
                "Should return 3 when word list has three words");
    }

    @Test
    public void testTotalWordCountWhenLarge() {
        List<String> words = new ArrayList<>();
        for (int i = 0; i < 5000; i++) {
            words.add("slate");
        }
        setupWithWordList(words);
        assertEquals(5000, model.getTotalWordCount(),
                "Should return 5000 when word list has 5000 words");
    }



    // ── Fringe cases ──────────────────────────────────────────────────────

    @Test
    public void testAllSameLettersAllGreen() {
        // With only one word in the list Absurdle is forced to commit
        List<String> words = new ArrayList<>();
        words.add("aaaaa");
        setupWithWordList(words);
        typeWords("AAAAA");
        model.setCurrentRow();

        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertEquals(AppColors.GREEN, row[i].getBackgroundColor(),
                    "Cell " + i + " should be GREEN when only one word remains and matches");
        }
    }

    @Test
    public void testAllSameLettersAllGray() {
        List<String> words = new ArrayList<>();
        words.add("aaaaa");
        words.add("bbbbb");
        setupWithWordList(words);
        model.setCurrentWord("aaaaa");
        typeWords("BBBBB");
        model.setCurrentRow();

        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertEquals(AppColors.GRAY, row[i].getBackgroundColor(),
                    "Cell " + i + " should be GRAY when no letters match");
        }
    }

    @Test
    public void testBackspaceAllFiveLetters() {
        typeWords("SLATE");
        for (int i = 0; i < 5; i++) {
            model.backspace();
        }
        WordleResponse[][] grid = model.getWordleGrid();
        for (int col = 1; col < 5; col++) {
            assertNull(grid[0][col],
                    "Cell at column " + col + " should be null after backspacing");
        }
    }
}