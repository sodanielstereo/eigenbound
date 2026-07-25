package com.eigenbound.domain.generation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.challenge.ChallengeStatus;

class VectorChallengeGeneratorTest {

    private final VectorChallengeGenerator generator = new VectorChallengeGenerator();

    @Test
    void generatedSolutionShouldSolveChallenge() {
        GeneratedVectorChallenge generated = generator.generate(2026L, 3);

        ChallengeStatus status = generated.challenge()
                .evaluate(generated.solution())
                .status();

        assertEquals(ChallengeStatus.SOLVED, status);
    }

    @Test
    void sameSeedShouldGenerateSameResult() {
        GeneratedVectorChallenge first = generator.generate(2026L, 3);

        GeneratedVectorChallenge second = generator.generate(2026L, 3);

        assertEquals(first, second);
    }

    @Test
    void differentSeedsShouldGenerateDifferentChallenges() {
        GeneratedVectorChallenge first = generator.generate(100L, 3);

        GeneratedVectorChallenge second = generator.generate(200L, 3);

        assertNotEquals(
                first.challenge(),
                second.challenge());
    }

    @Test
    void generatedMovementsShouldNeverBeZero() {
        for (long seed = 0; seed < 100; seed++) {
            GeneratedVectorChallenge generated = generator.generate(seed, 5);

            boolean containsZero = generated.challenge()
                    .availableMoves()
                    .stream()
                    .anyMatch(vector -> vector.isZero());

            assertFalse(
                    containsZero,
                    "Zero movement generated with seed " + seed);
        }
    }

    @Test
    void solutionShouldRespectMaximumSteps() {
        for (int difficulty = 1; difficulty <= 5; difficulty++) {

            GeneratedVectorChallenge generated = generator.generate(2026L, difficulty);

            assertTrue(
                    generated.solution().size() <= generated.challenge().maxSteps());
        }
    }

    @Test
    void difficultyShouldControlAvailableMovementCount() {
        assertEquals(
                2,
                generator.generate(1L, 1)
                        .challenge()
                        .availableMoves()
                        .size());

        assertEquals(
                2,
                generator.generate(1L, 2)
                        .challenge()
                        .availableMoves()
                        .size());

        assertEquals(
                3,
                generator.generate(1L, 3)
                        .challenge()
                        .availableMoves()
                        .size());

        assertEquals(
                3,
                generator.generate(1L, 4)
                        .challenge()
                        .availableMoves()
                        .size());

        assertEquals(
                4,
                generator.generate(1L, 5)
                        .challenge()
                        .availableMoves()
                        .size());
    }

    @Test
    void shouldRejectDifficultyBelowMinimum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(2026L, 0));
    }

    @Test
    void shouldRejectDifficultyAboveMaximum() {
        assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(2026L, 6));
    }

    @Test
    void shouldGenerateSolvableChallengesForManySeeds() {
        for (long seed = 0; seed < 1_000; seed++) {
            GeneratedVectorChallenge generated = generator.generate(seed, 3);

            ChallengeStatus status = generated.challenge()
                    .evaluate(generated.solution())
                    .status();

            assertEquals(
                    ChallengeStatus.SOLVED,
                    status,
                    "Unsolvable challenge generated with seed "
                            + seed);
        }
    }

    @Test
    void targetShouldDifferFromStartingPosition() {
        for (long seed = 0; seed < 100; seed++) {
            GeneratedVectorChallenge generated = generator.generate(seed, 3);

            assertNotEquals(
                    generated.challenge().start(),
                    generated.challenge().target(),
                    "Target equals start for seed " + seed);
        }
    }
}