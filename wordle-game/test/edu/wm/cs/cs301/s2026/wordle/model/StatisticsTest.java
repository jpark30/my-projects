package edu.wm.cs.cs301.s2026.wordle.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//Generated with Claude
/**
 * Unit tests for {@link Statistics}.
 *
 * <p>Because {@code Statistics} hard-codes the log path to
 * {@code ~/Wordle/statistics.log}, each test redirects the {@code user.home}
 * system property to a JUnit {@code @TempDir} so the real home directory is
 * never touched and every test starts with a clean slate.</p>
 */
@DisplayName("Statistics")
class StatisticsTest {

    // The original home dir so we can restore it after every test
    private String originalHome;

    @TempDir
    Path tempDir;

    // Convenience: the Wordle sub-directory inside the temp home
    private Path wordleDir;
    private Path logFile;

    @BeforeEach
    void redirectHomeToTempDir() {
        originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toString());

        wordleDir = tempDir.resolve("Wordle");
        logFile   = wordleDir.resolve("statistics.log");
    }

    @AfterEach
    void restoreHome() {
        System.setProperty("user.home", originalHome);
    }

    // -----------------------------------------------------------------------
    // Helper – write a statistics.log with the given values
    // -----------------------------------------------------------------------

    private void writeLogFile(int currentStreak,
                               int longestStreak,
                               int totalGamesPlayed,
                               List<Integer> wordsGuessed) throws IOException {
        Files.createDirectories(wordleDir);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile.toFile()))) {
            bw.write(currentStreak    + System.lineSeparator());
            bw.write(longestStreak    + System.lineSeparator());
            bw.write(totalGamesPlayed + System.lineSeparator());
            bw.write(wordsGuessed.size() + System.lineSeparator());
            for (int v : wordsGuessed) {
                bw.write(v + System.lineSeparator());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Construction / first run
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("Construction")
    class ConstructionTests {

        @Test
        @DisplayName("First run (no log file) initialises all counters to zero")
        void firstRun_allCountersAreZero() {
            Statistics stats = new Statistics();

            assertAll(
                    () -> assertEquals(0, stats.getCurrentStreak(),    "currentStreak"),
                    () -> assertEquals(0, stats.getLongestStreak(),    "longestStreak"),
                    () -> assertEquals(0, stats.getTotalGamesPlayed(), "totalGamesPlayed"),
                    () -> assertTrue(stats.getWordsGuessed().isEmpty(), "wordsGuessed should be empty")
            );
        }

        @Test
        @DisplayName("Existing log file is read and fields are populated correctly")
        void existingLogFile_fieldsPopulated() throws IOException {
            writeLogFile(3, 7, 10, List.of(1, 2, 3, 4));

            Statistics stats = new Statistics();

            assertAll(
                    () -> assertEquals(3,  stats.getCurrentStreak()),
                    () -> assertEquals(7,  stats.getLongestStreak()),
                    () -> assertEquals(10, stats.getTotalGamesPlayed()),
                    () -> assertEquals(List.of(1, 2, 3, 4), stats.getWordsGuessed())
            );
        }

        @Test
        @DisplayName("Log file with empty wordsGuessed list is handled correctly")
        void logFile_emptyWordsGuessed() throws IOException {
            writeLogFile(1, 1, 1, List.of());

            Statistics stats = new Statistics();

            assertTrue(stats.getWordsGuessed().isEmpty());
        }

        @Test
        @DisplayName("Log file with a single wordsGuessed entry is parsed correctly")
        void logFile_singleWordsGuessedEntry() throws IOException {
            writeLogFile(1, 2, 5, List.of(4));

            Statistics stats = new Statistics();

            assertEquals(List.of(4), stats.getWordsGuessed());
        }
        @Test
        @DisplayName("FileNotFoundException is handled silently - counters default to zero")
        void readStatistics_fileNotFound_countersAreZero() {
            // tempDir has no statistics.log — FileNotFoundException path is exercised
            Statistics stats = new Statistics();

            assertAll(
                () -> assertEquals(0, stats.getCurrentStreak()),
                () -> assertEquals(0, stats.getLongestStreak()),
                () -> assertEquals(0, stats.getTotalGamesPlayed())
            );
        }

    }
	    

    // -----------------------------------------------------------------------
    // setCurrentStreak / longestStreak auto-update
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("setCurrentStreak()")
    class SetCurrentStreakTests {

        @Test
        @DisplayName("Updates currentStreak to the supplied value")
        void setsCurrentStreakValue() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(5);
            assertEquals(5, stats.getCurrentStreak());
        }

        @Test
        @DisplayName("Automatically updates longestStreak when new value exceeds it")
        void updatesLongestStreakWhenExceeded() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(4);
            assertEquals(4, stats.getLongestStreak());

            stats.setCurrentStreak(9);
            assertEquals(9, stats.getLongestStreak());
        }

        @Test
        @DisplayName("Does NOT decrease longestStreak when current streak resets to 0")
        void longestStreakDoesNotDecreaseOnReset() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(6);
            assertEquals(6, stats.getLongestStreak());

            stats.setCurrentStreak(0); // simulate a loss
            assertEquals(0, stats.getCurrentStreak());
            assertEquals(6, stats.getLongestStreak(), "longestStreak should remain 6 after reset");
        }

        @Test
        @DisplayName("longestStreak unchanged when new streak is less than existing record")
        void longestStreakUnchangedWhenNotExceeded() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(5);
            stats.setCurrentStreak(3); // smaller than 5
            assertEquals(5, stats.getLongestStreak());
        }

        @Test
        @DisplayName("longestStreak unchanged when new streak equals existing record")
        void longestStreakUnchangedWhenEqual() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(5);
            stats.setCurrentStreak(5);
            assertEquals(5, stats.getLongestStreak());
        }

        @ParameterizedTest(name = "streak = {0}")
        @ValueSource(ints = {0, 1, 5, 100})
        @DisplayName("Handles various streak values without error")
        void handlesVariousStreakValues(int streak) {
            Statistics stats = new Statistics();
            assertDoesNotThrow(() -> stats.setCurrentStreak(streak));
        }
    }

    // -----------------------------------------------------------------------
    // incrementTotalGamesPlayed
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("incrementTotalGamesPlayed()")
    class IncrementTotalGamesPlayedTests {

        @Test
        @DisplayName("Increments from 0 to 1 on first call")
        void firstIncrement_isOne() {
            Statistics stats = new Statistics();
            stats.incrementTotalGamesPlayed();
            assertEquals(1, stats.getTotalGamesPlayed());
        }

        @Test
        @DisplayName("Accumulates correctly over multiple increments")
        void multipleIncrements_accumulateCorrectly() {
            Statistics stats = new Statistics();
            for (int i = 0; i < 10; i++) {
                stats.incrementTotalGamesPlayed();
            }
            assertEquals(10, stats.getTotalGamesPlayed());
        }
    }

    // -----------------------------------------------------------------------
    // addWordsGuessed
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("addWordsGuessed()")
    class AddWordsGuessedTests {

        @Test
        @DisplayName("Adds a single entry to the list")
        void addsOneEntry() {
            Statistics stats = new Statistics();
            stats.addWordsGuessed(3);
            assertEquals(List.of(3), stats.getWordsGuessed());
        }

        @Test
        @DisplayName("Appends multiple entries in order")
        void appendsMultipleEntriesInOrder() {
            Statistics stats = new Statistics();
            stats.addWordsGuessed(1);
            stats.addWordsGuessed(4);
            stats.addWordsGuessed(2);
            assertEquals(List.of(1, 4, 2), stats.getWordsGuessed());
        }

        @ParameterizedTest(name = "guesses = {0}")
        @ValueSource(ints = {1, 2, 3, 4, 5, 6})
        @DisplayName("Accepts all valid guess-count values (1–6)")
        void acceptsValidGuessCounts(int guesses) {
            Statistics stats = new Statistics();
            assertDoesNotThrow(() -> stats.addWordsGuessed(guesses));
            assertTrue(stats.getWordsGuessed().contains(guesses));
        }
    }

    // -----------------------------------------------------------------------
    // writeStatistics / round-trip persistence
    // -----------------------------------------------------------------------

    @Nested
    @DisplayName("writeStatistics() persistence")
    class WriteStatisticsTests {

        @Test
        @DisplayName("Creates the Wordle directory if it does not exist")
        void createsWordleDirectory() {
            Statistics stats = new Statistics();
            stats.writeStatistics();
            assertTrue(wordleDir.toFile().isDirectory(), "~/Wordle directory should be created");
        }

        @Test
        @DisplayName("Creates the statistics.log file if it does not exist")
        void createsLogFile() {
            Statistics stats = new Statistics();
            stats.writeStatistics();
            assertTrue(logFile.toFile().exists(), "statistics.log should be created");
        }

        @Test
        @DisplayName("Round-trip: written values are read back identically by a new instance")
        void roundTrip_valuesPersistedAndRestored() {
            Statistics original = new Statistics();
            original.setCurrentStreak(4);
            original.incrementTotalGamesPlayed();
            original.incrementTotalGamesPlayed();
            original.addWordsGuessed(3);
            original.addWordsGuessed(5);
            original.writeStatistics();

            // New instance reads the file that was just written
            Statistics restored = new Statistics();

            assertAll(
                    () -> assertEquals(original.getCurrentStreak(),    restored.getCurrentStreak(),    "currentStreak"),
                    () -> assertEquals(original.getLongestStreak(),    restored.getLongestStreak(),    "longestStreak"),
                    () -> assertEquals(original.getTotalGamesPlayed(), restored.getTotalGamesPlayed(), "totalGamesPlayed"),
                    () -> assertEquals(original.getWordsGuessed(),     restored.getWordsGuessed(),     "wordsGuessed")
            );
        }

        @Test
        @DisplayName("Round-trip with empty wordsGuessed list persists correctly")
        void roundTrip_emptyWordsGuessed() {
            Statistics original = new Statistics();
            original.setCurrentStreak(2);
            original.incrementTotalGamesPlayed();
            original.writeStatistics();

            Statistics restored = new Statistics();
            assertTrue(restored.getWordsGuessed().isEmpty());
        }

        @Test
        @DisplayName("Overwriting an existing log file replaces old values")
        void overwrite_replacesOldValues() throws IOException {
            writeLogFile(1, 5, 8, List.of(2, 3));

            Statistics stats = new Statistics();
            // stats reads: currentStreak=1, longestStreak=5, totalGamesPlayed=8, wordsGuessed=[2,3]
            stats.setCurrentStreak(0);           // currentStreak → 0, longestStreak stays 5
            stats.incrementTotalGamesPlayed();   // totalGamesPlayed → 9
            stats.writeStatistics();

            Statistics restored = new Statistics();
            assertAll(
                () -> assertEquals(0,           restored.getCurrentStreak(),    "currentStreak overwritten to 0"),
                () -> assertEquals(5,           restored.getLongestStreak(),    "longestStreak unchanged at 5"),
                () -> assertEquals(9,           restored.getTotalGamesPlayed(), "totalGamesPlayed was 8, incremented to 9"),
                () -> assertEquals(List.of(2,3),restored.getWordsGuessed(),    "wordsGuessed unchanged")
            );
        }

        @Test
        @DisplayName("writeStatistics() is idempotent: writing twice gives the same result")
        void idempotent_writeTwiceGivesSameResult() {
            Statistics stats = new Statistics();
            stats.setCurrentStreak(3);
            stats.addWordsGuessed(2);
            stats.writeStatistics();
            stats.writeStatistics(); // second write

            Statistics restored = new Statistics();
            assertEquals(3, restored.getCurrentStreak());
            assertEquals(List.of(2), restored.getWordsGuessed());
        }
    }

    // -----------------------------------------------------------------------
    // getWordsGuessed() list is live (not a copy)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("getWordsGuessed() returns the live internal list (reflects later additions)")
    void getWordsGuessed_isLiveList() {
        Statistics stats = new Statistics();
        List<Integer> ref = stats.getWordsGuessed();
        stats.addWordsGuessed(4);
        assertTrue(ref.contains(4), "getWordsGuessed() should reflect additions made after it was called");
    }
}