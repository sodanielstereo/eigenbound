package com.eigenbound.domain.puzzle.cryptography;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.puzzle.MiniPuzzle;
import com.eigenbound.domain.puzzle.PuzzleOption;
import com.eigenbound.domain.puzzle.PuzzleTopic;
import com.eigenbound.domain.puzzle.generation.GeneratedMiniPuzzle;

class CaesarCipherPuzzleGeneratorTest {

    private static final Pattern PROMPT_PATTERN = Pattern.compile(
            "^Decrypt this Caesar message using shift (\\d+): ([A-Z ]+)$");

    private CaesarCipher cipher;
    private CaesarCipherPuzzleGenerator generator;

    @BeforeEach
    void setUp() {
        cipher = new CaesarCipher();
        generator = new CaesarCipherPuzzleGenerator(
                cipher);
    }

    @Test
    void shouldGenerateCryptographyPuzzle() {
        GeneratedMiniPuzzle generated = generator.generate(
                73L,
                1);

        assertEquals(
                PuzzleTopic.CRYPTOGRAPHY,
                generator.topic());
        assertEquals(
                PuzzleTopic.CRYPTOGRAPHY,
                generated.puzzle().topic());
    }

    @Test
    void shouldPreserveGenerationSeed() {
        GeneratedMiniPuzzle generated = generator.generate(
                73L,
                2);

        assertEquals(73L, generated.seed());
    }

    @Test
    void shouldGenerateSamePuzzleFromSameSettings() {
        GeneratedMiniPuzzle first = generator.generate(
                73L,
                3);

        GeneratedMiniPuzzle second = generator.generate(
                73L,
                3);

        assertEquals(first, second);
    }

    @Test
    void shouldGenerateVarietyAcrossSeeds() {
        Set<String> generatedPrompts = new HashSet<>();

        for (long seed = 0; seed < 100; seed++) {
            generatedPrompts.add(
                    generator.generate(
                            seed,
                            3)
                            .puzzle()
                            .prompt());
        }

        assertTrue(generatedPrompts.size() >= 20);
    }

    @Test
    void shouldCreateFourUniqueOptions() {
        MiniPuzzle puzzle = generator.generate(
                73L,
                2)
                .puzzle();

        Set<String> optionIds = new HashSet<>();
        Set<String> optionTexts = new HashSet<>();

        for (PuzzleOption option : puzzle.options()) {
            optionIds.add(option.id());
            optionTexts.add(option.text());
        }

        assertEquals(4, puzzle.options().size());
        assertEquals(4, optionIds.size());
        assertEquals(4, optionTexts.size());
    }

    @Test
    void shouldReduceTimeAsDifficultyIncreases() {
        Duration easiestTime = generator.generate(
                73L,
                1)
                .puzzle()
                .timeLimit();

        Duration hardestTime = generator.generate(
                73L,
                5)
                .puzzle()
                .timeLimit();

        assertEquals(Duration.ofSeconds(23), easiestTime);
        assertEquals(Duration.ofSeconds(11), hardestTime);
        assertTrue(hardestTime.compareTo(easiestTime) < 0);
    }

    @Test
    void shouldGenerateLongerContentAtHighestDifficulty() {
        MiniPuzzle easiest = generator.generate(
                73L,
                1)
                .puzzle();

        MiniPuzzle hardest = generator.generate(
                73L,
                5)
                .puzzle();

        String easiestAnswer = correctAnswer(easiest);
        String hardestAnswer = correctAnswer(hardest);

        assertTrue(
                hardestAnswer.length() > easiestAnswer.length());
    }

    @Test
    void shouldGenerateSolvablePuzzlesAcrossSeedsAndDifficulties() {
        for (int difficulty = 1; difficulty <= 5; difficulty++) {
            for (long seed = 0; seed < 250; seed++) {
                MiniPuzzle puzzle = generator.generate(
                        seed,
                        difficulty)
                        .puzzle();

                Matcher matcher = PROMPT_PATTERN.matcher(
                        puzzle.prompt());

                assertTrue(
                        matcher.matches(),
                        "Unexpected prompt for seed "
                                + seed
                                + " and difficulty "
                                + difficulty);

                int shift = Integer.parseInt(
                        matcher.group(1));

                String encryptedMessage = matcher.group(2);

                assertEquals(
                        correctAnswer(puzzle),
                        cipher.decrypt(
                                encryptedMessage,
                                shift));
            }
        }
    }

    @Test
    void shouldGenerateDifferentPuzzleIdsForDifferentSettings() {
        String firstId = generator.generate(
                73L,
                1)
                .puzzle()
                .id();

        String secondId = generator.generate(
                74L,
                1)
                .puzzle()
                .id();

        String thirdId = generator.generate(
                73L,
                2)
                .puzzle()
                .id();

        assertNotEquals(firstId, secondId);
        assertNotEquals(firstId, thirdId);
    }

    @Test
    void shouldRejectDifficultyBelowMinimum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(
                        73L,
                        0));
    }

    @Test
    void shouldRejectDifficultyAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(
                        73L,
                        6));
    }

    @Test
    void shouldRejectNullCipher() {
        assertThrows(
                NullPointerException.class,
                () -> new CaesarCipherPuzzleGenerator(null));
    }

    private String correctAnswer(
            MiniPuzzle puzzle) {
        return puzzle.options()
                .stream()
                .filter(
                        option -> option.id()
                                .equals(
                                        puzzle.correctOptionId()))
                .findFirst()
                .orElseThrow()
                .text();
    }
}