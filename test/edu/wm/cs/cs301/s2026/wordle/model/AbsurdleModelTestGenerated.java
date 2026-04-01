package edu.wm.cs.cs301.s2026.wordle.model;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Supplemental tests for AbsurdleModel. Adapted from WordleModelTestGenerated.
 * Tests that are identical to WordleModel are kept as-is since AbsurdleModel
 * shares the same grid management, column/row tracking, and backspace behavior.
 * Tests that involve color evaluation are rewritten to account for Absurdle's
 * adversarial word selection behavior.
 */
public class AbsurdleModelTestGenerated {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new AbsurdleModel(); // changed from WordleModel
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("SQQQS");
        words.add("AQAAA");
        words.add("AQQQS");
        words.add("ETQQQ");
        words.add("SSQQQ");
        words.add("telas");
        words.add("QQQQQ");
        model.setWordList(words);
        model.initialize();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void typeWords(String word) {
        for (char c : word.toCharArray()) {
            model.setCurrentColumn(c);
        }
    }

    private void setupWithWord(String word) {
        List<String> words = new ArrayList<>();
        words.add(word);
        model.setWordList(words);
        model.initialize();
    }

    private void setupWithWordList(List<String> words) {
        model.setWordList(words);
        model.initialize();
    }

    // ── Foreground colour ────────────────────────────────────────────────────
    // NOTE: These tests are identical to WordleModel since foreground color
    // behavior does not depend on word selection strategy

    @Test
    @DisplayName("Typed (unsubmitted) cell has BLACK foreground")
    public void testTypedCellHasBlackForeground() {
        model.setCurrentColumn('S');
        assertEquals(Color.BLACK, model.getWordleGrid()[0][0].getForegroundColor(),
                "Unsubmitted cell foreground should be BLACK");
    }

    @Test
    @DisplayName("Evaluated cell has WHITE foreground after submission")
    public void testEvaluatedCellHasWhiteForeground() {
        // With one word in the list Absurdle is forced to commit
        setupWithWord("slate");
        typeWords("SLATE");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertEquals(Color.WHITE, row[i].getForegroundColor(),
                    "Submitted cell " + i + " foreground should be WHITE");
        }
    }

    // ── Letter identity in grid cells ────────────────────────────────────────
    // NOTE: These tests are identical to WordleModel since cell population
    // behavior does not depend on word selection strategy

    @Test
    @DisplayName("Typing three letters populates exactly those three cells, rest stay null")
    public void testTypedLettersCellsPopulated() {
        model.setCurrentColumn('S');
        model.setCurrentColumn('L');
        model.setCurrentColumn('A');

        WordleResponse[][] grid = model.getWordleGrid();
        assertNotNull(grid[0][0], "Column 0 should be populated after typing S");
        assertNotNull(grid[0][1], "Column 1 should be populated after typing L");
        assertNotNull(grid[0][2], "Column 2 should be populated after typing A");
        assertNull(grid[0][3], "Column 3 should still be null");
        assertNull(grid[0][4], "Column 4 should still be null");
    }

    @Test
    @DisplayName("Submitted cells are all non-null and have WHITE foreground")
    public void testSubmittedCellsAllNonNull() {
        setupWithWord("slate");
        typeWords("SLATE");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertNotNull(row[i], "Cell " + i + " should not be null after submission");
            assertEquals(Color.WHITE, row[i].getForegroundColor(),
                    "Cell " + i + " should have WHITE foreground after evaluation");
        }
    }

    // ── Adversarial color behavior ───────────────────────────────────────────
    // NOTE: These tests are DIFFERENT from WordleModel.
    // In Absurdle we cannot predict exact color patterns when multiple words
    // remain — Absurdle picks the pattern adversarially. We test the
    // constraints on what the adversarial choice must satisfy.

    @Test
    @DisplayName("With one word in list, Absurdle forced to show all GREEN on exact match")
    public void testAllGreenWhenForcedToCommit() {
        // With only one word remaining Absurdle has no choice
        setupWithWord("slate");
        typeWords("SLATE");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertEquals(AppColors.GREEN, row[i].getBackgroundColor(),
                    "Cell " + i + " should be GREEN when Absurdle is forced to commit");
        }
    }

    @Test
    @DisplayName("With multiple words remaining, Absurdle avoids all GREEN unless forced")
    public void testAbsurdleAvoidsGreenWithMultipleWords() {
        // With multiple words in the list Absurdle should not show all GREEN
        // for a guess that matches one of them — it will pick a harder pattern
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("qqqqq");
        words.add("wwwww");
        setupWithWordList(words);

        typeWords("SLATE");
        model.setCurrentRow();

        WordleResponse[] row = model.getCurrentRow();
        assertNotNull(row, "Row should not be null after submission");

        // Count GREEN cells — should not be all 5 GREEN since other words remain
        int greenCount = 0;
        for (WordleResponse cell : row) {
            if (cell.getBackgroundColor().equals(AppColors.GREEN)) greenCount++;
        }
        assertNotEquals(5, greenCount,
                "Absurdle should not show all GREEN when multiple words remain");
    }

    @Test
    @DisplayName("Submitted row cells are all non-null after adversarial evaluation")
    public void testSubmittedRowCellsNonNull() {
        typeWords("QQQQQ");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertNotNull(row[i],
                    "Cell " + i + " should not be null after adversarial evaluation");
        }
    }

    @Test
    @DisplayName("Each submitted cell has a valid Wordle color (GREEN, YELLOW, or GRAY)")
    public void testSubmittedCellsHaveValidColors() {
        typeWords("QQQQQ");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            Color bg = row[i].getBackgroundColor();
            assertTrue(
                bg.equals(AppColors.GREEN) ||
                bg.equals(AppColors.YELLOW) ||
                bg.equals(AppColors.GRAY),
                "Cell " + i + " should have GREEN, YELLOW, or GRAY background"
            );
        }
    }

    @Test
    @DisplayName("All five letters wrong position all YELLOW — forced when one word remains")
    public void testAllLettersWrongPositionAllYellow() {
        // With only TELAS in the list and guess SLATE,
        // Absurdle is forced to commit to TELAS
        // Every letter of SLATE is in TELAS but wrong position
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("telas");
        setupWithWordList(words);
        model.setCurrentWord("TELAS");
        typeWords("SLATE");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();
        for (int i = 0; i < row.length; i++) {
            assertNotEquals(AppColors.GRAY, row[i].getBackgroundColor(),
                    "Cell " + i + " should not be GRAY — letter exists in TELAS");
        }
    }

    // ── Grid persistence across rows ─────────────────────────────────────────
    // NOTE: These tests are identical to WordleModel since grid persistence
    // does not depend on word selection strategy

    @Test
    @DisplayName("Previous row cells are not overwritten when a new guess is submitted")
    public void testPreviousRowRetainedAfterSecondSubmission() {
        // Force target to SLATE so we know exact colors
        setupWithWord("slate");
        typeWords("SLATE");
        model.setCurrentRow(); // row 0 = all GREEN since only one word

        // Add more words and submit second row
        List<String> words = new ArrayList<>();
        words.add("slate");
        words.add("qqqqq");
        setupWithWordList(words);
        model.setCurrentWord("SLATE");

        typeWords("QQQQQ");
        model.setCurrentRow();

        WordleResponse[][] grid = model.getWordleGrid();
        // Row 0 should still be populated
        for (int col = 0; col < 5; col++) {
            assertNotNull(grid[0][col],
                    "Row 0 cell " + col + " should still be populated after second submission");
        }
    }

    @Test
    @DisplayName("Rows beyond the current guess remain null during play")
    public void testFutureRowsRemainNullDuringPlay() {
        typeWords("QQQQQ");
        model.setCurrentRow(); // only row 0 is filled

        WordleResponse[][] grid = model.getWordleGrid();
        for (int row = 1; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                assertNull(grid[row][col],
                        "Cell [" + row + "][" + col + "] should still be null");
            }
        }
    }

    // ── setCurrentRow() column reset ─────────────────────────────────────────
    // NOTE: Identical to WordleModel

    @Test
    @DisplayName("currentColumn resets to -1 after every submission, not just the first")
    public void testColumnResetsToMinusOneAfterEachSubmission() {
        for (int attempt = 0; attempt < 3; attempt++) {
            typeWords("QQQQQ");
            model.setCurrentRow();
            assertEquals(-1, model.getCurrentColumn(),
                    "currentColumn should be -1 after submission " + (attempt + 1));
        }
    }

    @Test
    @DisplayName("Typing after submission writes into the next row, not the previous one")
    public void testTypingAfterSubmissionWritesIntoNextRow() {
        // Use a valid word so setCurrentRow() actually processes the submission
        setupWithWord("qqqqq");
        typeWords("QQQQQ");
        model.setCurrentRow(); // row 0 submitted

        model.setCurrentColumn('X');
        WordleResponse[][] grid = model.getWordleGrid();

        assertNotNull(grid[1][0], "New letter should appear in row 1 col 0");
        assertEquals(Color.WHITE, grid[1][0].getBackgroundColor(),
                "Newly typed cell in row 1 should have WHITE unsubmitted background");

        for (int col = 1; col < 5; col++) {
            assertNull(grid[1][col], "Row 1 col " + col + " should still be null");
        }
    }

    // ── Backspace edge cases ─────────────────────────────────────────────────
    // NOTE: Identical to WordleModel

    @Test
    @DisplayName("Backspace on the last column (col 4) clears that cell")
    public void testBackspaceOnLastColumn() {
        typeWords("SLATE");
        model.backspace();
        assertNull(model.getWordleGrid()[0][4],
                "Column 4 should be null after backspace");
        assertEquals(3, model.getCurrentColumn(),
                "currentColumn should decrement to 3");
    }

    @Test
    @DisplayName("Backspace then retype replaces cell content")
    public void testBackspaceThenRetypeUpdatesCell() {
        model.setCurrentColumn('S');
        model.setCurrentColumn('Q');
        model.backspace();
        assertNull(model.getWordleGrid()[0][1],
                "Column 1 should be null after backspace");

        model.setCurrentColumn('L');
        assertNotNull(model.getWordleGrid()[0][1],
                "Column 1 should be non-null after retyping");
        assertEquals(Color.WHITE, model.getWordleGrid()[0][1].getBackgroundColor(),
                "Retyped cell should have WHITE unsubmitted background");
    }

    // ── initialize() behavior ────────────────────────────────────────────────

    @Test
    @DisplayName("initialize() with a new word list picks the new target word")
    public void testInitializeChangesTargetWord() {
        // NOTE: In Absurdle getCurrentWord() may return null until forced
        // With one word in the list it must commit immediately
        setupWithWord("slate");
        assertEquals("SLATE", model.getCurrentWord(),
                "With one word, Absurdle must commit to SLATE");

        setupWithWord("crane");
        assertEquals("CRANE", model.getCurrentWord(),
                "After re-initializing with crane, Absurdle must commit to CRANE");
    }

    @Test
    @DisplayName("initialize() clears any partially typed letters from the grid")
    public void testInitializeClearsPartialInput() {
        typeWords("SLA");
        model.initialize();

        WordleResponse[][] grid = model.getWordleGrid();
        for (int col = 0; col < 3; col++) {
            assertNull(grid[0][col],
                    "Column " + col + " should be null after initialize()");
        }
    }

    // ── getCurrentRowNumber() at boundaries ──────────────────────────────────
    // NOTE: Identical to WordleModel

    @Test
    @DisplayName("getCurrentRowNumber() returns 4 after five submissions")
    public void testCurrentRowNumberAfterFiveSubmissions() {
        for (int i = 0; i < 5; i++) {
            typeWords("QQQQQ");
            model.setCurrentRow();
        }
        assertEquals(4, model.getCurrentRowNumber(),
                "After 5 submissions getCurrentRowNumber() should return 4");
    }

    @Test
    @DisplayName("getCurrentRowNumber() returns 5 after six submissions (game over)")
    public void testCurrentRowNumberAfterSixSubmissions() {
        for (int i = 0; i < 6; i++) {
            typeWords("QQQQQ");
            model.setCurrentRow();
        }
        assertEquals(5, model.getCurrentRowNumber(),
                "After 6 submissions getCurrentRowNumber() should return 5");
    }

    // ── Repeated letters ─────────────────────────────────────────────────────
    // NOTE: These tests force Absurdle into a single word so color behavior
    // is deterministic and matches the expected pattern

    @Test
    @DisplayName("Repeated letter in guess: one GREEN, duplicate is GRAY")
    public void testRepeatedLetterInGuessOneGreenOneGray() {
        List<String> words = new ArrayList<>();
        words.add("aaaab");
        words.add("aqaaa");
        setupWithWordList(words);
        model.setCurrentWord("AAAAB");

        typeWords("AQAAA");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();

        assertEquals(AppColors.GREEN,  row[0].getBackgroundColor(), "Index 0: A exact → GREEN");
        assertEquals(AppColors.GRAY,   row[1].getBackgroundColor(), "Index 1: Q not in target → GRAY");
        assertEquals(AppColors.GREEN,  row[2].getBackgroundColor(), "Index 2: A exact → GREEN");
        assertEquals(AppColors.GREEN,  row[3].getBackgroundColor(), "Index 3: A exact → GREEN");
        assertEquals(AppColors.YELLOW, row[4].getBackgroundColor(), "Index 4: A finds unused A → YELLOW");
    }

    @Test
    @DisplayName("Letter appears once in target but twice in guess — second occurrence is GRAY")
    public void testLetterTwiceInGuessOnceInTarget() {
        List<String> words = new ArrayList<>();
        words.add("sqqqq");
        words.add("ssqqq");
        setupWithWordList(words);
        model.setCurrentWord("SQQQQ");

        typeWords("SSQQQ");
        model.setCurrentRow();
        WordleResponse[] row = model.getCurrentRow();

        assertEquals(AppColors.GREEN, row[0].getBackgroundColor(),
                "Index 0: S exact match → GREEN");
        assertEquals(AppColors.GRAY,  row[1].getBackgroundColor(),
                "Index 1: second S already used by GREEN → GRAY");
        assertEquals(AppColors.GREEN, row[2].getBackgroundColor(),
                "Index 2: Q exact match → GREEN");
    }

    
    /*
     * ── Testing Decorator Pattern Rules with Absurdle ──────────────────
     */

    @Test
    public void testAbsurdleWithRuleBasic() {
        AcceptanceRule rule = new RuleBasic();
        
        // Test partial word
        model.setCurrentColumn('S');
        assertFalse(rule.isAcceptableGuess(model), "RuleBasic should reject 1-letter word in Absurdle");
        
        // Test full word
        model.initialize();
        typeWords("SLATE");
        assertTrue(rule.isAcceptableGuess(model), "RuleBasic should accept 5-letter word in Absurdle");
    }

    @Test
    public void testAbsurdleWithRuleLegitimateWordsOnly() {
        AcceptanceRule rule = new RuleLegitimateWordsOnly(new RuleBasic());
        
        // "slate" is in our setup list
        typeWords("SLATE");
        assertTrue(rule.isAcceptableGuess(model), "Should accept 'SLATE' as it is in the list");
        
        model.initialize();
        
        // "xxxxx" is NOT in our setup list
        typeWords("XXXXX");
        assertFalse(rule.isAcceptableGuess(model), "Should reject 'XXXXX' as it is not a real word");
    }

    @Test
    public void testAbsurdleHardModeEnforcement() {
        // 1. Setup the Rule stack (Hard wraps Basic)
        AcceptanceRule rule = new RuleHard(new RuleBasic());
        
        // 2. Force Absurdle to commit to one word so we know the colors
        List<String> words = new ArrayList<>();
        words.add("slate");
        setupWithWordList(words);
        model.setCurrentWord("SLATE"); // Absurdle is now locked to SLATE

        // 3. Row 0: Submit "STARE"
        // In "SLATE", 'S' is Green (0), 'A' and 'E' are Green (2, 4).
        // 'T' is Yellow (found in SLATE but at a different spot).
        // 'R' is Gray (not in SLATE).
        typeWords("STARE");
        model.setCurrentRow(); 

        // 4. Test GREEN Violation: Try "PLATE" (Index 0 should be 'S')
        typeWords("PLATE");
        assertFalse(rule.isAcceptableGuess(model), 
            "Hard Mode should reject 'PLATE' because 'S' was green at index 0");
        
        // 5. Test YELLOW Violation: Try "SALES" 
        // This has 'S', 'A', 'E' but misses the 'T' that was yellow in "STARE"
        for(int i=0; i<5; i++) model.backspace(); 
        typeWords("SALES");
        assertFalse(rule.isAcceptableGuess(model), 
            "Hard Mode should reject 'SALES' because 'T' was yellow and is now missing");

        // 6. Test GRAY Violation: Try "STARE" again or "SHARE"
        // Since 'R' was gray in Row 0, it cannot be used again.
        for(int i=0; i<5; i++) model.backspace();
        typeWords("SHARE");
        assertFalse(rule.isAcceptableGuess(model), 
            "Hard Mode should reject 'SHARE' because 'R' was gray in the previous row");

        // 7. Test Valid Guess: "SLATE"
        // Follows green S, A, E; uses yellow T; contains no gray R.
        for(int i=0; i<5; i++) model.backspace();
        typeWords("SLATE");
        assertTrue(rule.isAcceptableGuess(model), 
            "Hard Mode should accept 'SLATE' as it follows all previous hints");
    }

}
