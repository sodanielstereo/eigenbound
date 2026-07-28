package com.eigenbound.domain.solver;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.generation.GeneratedVectorChallenge;
import com.eigenbound.domain.generation.VectorChallengeGenerator;
import com.eigenbound.domain.math.Vector2;

class VectorChallengeSolverTest {

    private final VectorChallengeSolver solver = new VectorChallengeSolver();

    @Test
    void shouldFindOneStepSolution() {
        Vector2 movement = new Vector2(2, 1);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(2, 1),
                List.of(movement),
                1);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertEquals(
                List.of(movement),
                solution.movements());
        assertEquals(1, solution.stepCount());
    }

    @Test
    void shouldFindMultiStepSolution() {
        Vector2 moveA = new Vector2(2, 1);
        Vector2 moveB = new Vector2(1, 2);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(5, 4),
                List.of(moveA, moveB),
                3);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertEquals(3, solution.stepCount());

        assertEquals(
                ChallengeStatus.SOLVED,
                challenge.evaluate(
                        solution.movements()).status());
    }

    @Test
    void shouldReturnShortestSolution() {
        Vector2 shortMovement = new Vector2(3, 0);

        Vector2 longMovement = new Vector2(1, 0);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(3, 0),
                List.of(
                        longMovement,
                        shortMovement),
                3);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertEquals(1, solution.stepCount());
        assertEquals(
                List.of(shortMovement),
                solution.movements());
    }

    @Test
    void shouldReturnEmptyForImpossibleChallenge() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(1, 0),
                List.of(new Vector2(2, 0)),
                3);

        Optional<ChallengeSolution> result = solver.solve(challenge);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRespectMaximumSteps() {
        Vector2 movement = new Vector2(1, 0);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(4, 0),
                List.of(movement),
                3);

        assertTrue(
                solver.solve(challenge).isEmpty());
    }

    @Test
    void shouldSolveWhenStartAlreadyEqualsTarget() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(2, 2),
                new Vector2(2, 2),
                List.of(new Vector2(1, 0)),
                3);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertTrue(solution.movements().isEmpty());
        assertEquals(0, solution.stepCount());
    }

    @Test
    void shouldRejectNullChallenge() {
        assertThrows(
                NullPointerException.class,
                () -> solver.solve(null));
    }

    @Test
    void solutionShouldReportExploredStates() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(2, 1),
                List.of(new Vector2(2, 1)),
                1);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertTrue(solution.exploredStates() > 0);
    }

    @Test
    void solutionShouldNotModifyChallenge() {
        Vector2 movement = new Vector2(2, 1);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(4, 2),
                List.of(movement),
                2);

        solver.solve(challenge);

        assertEquals(
                new Vector2(0, 0),
                challenge.start());
        assertEquals(
                new Vector2(4, 2),
                challenge.target());
        assertEquals(
                List.of(movement),
                challenge.availableMoves());
    }

    @Test
    void shouldSolveProcedurallyGeneratedChallenges() {
        VectorChallengeGenerator generator = new VectorChallengeGenerator();

        for (long seed = 0; seed < 250; seed++) {
            final long currentSeed = seed;

            int difficulty = (int) (currentSeed % 5) + 1;

            GeneratedVectorChallenge generated = generator.generate(
                    currentSeed,
                    difficulty);

            ChallengeSolution solution = solver.solve(
                    generated.challenge()).orElseThrow(
                            () -> new AssertionError(
                                    "No solution found for seed "
                                            + currentSeed));

            assertEquals(
                    ChallengeStatus.SOLVED,
                    generated.challenge()
                            .evaluate(solution.movements())
                            .status(),
                    "Invalid BFS solution for seed "
                            + currentSeed);

            assertTrue(
                    solution.stepCount() <= generated.challenge()
                            .maxSteps(),
                    "Step limit exceeded for seed "
                            + currentSeed);
        }
    }

    @Test
    void shouldCollapseEquivalentMovementOrders() {
        Vector2 moveA = new Vector2(2, 1);
        Vector2 moveB = new Vector2(1, 2);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(3, 3),
                List.of(moveA, moveB),
                2);

        ChallengeSolution solution = solver.solve(challenge).orElseThrow();

        assertEquals(2, solution.stepCount());

        assertFalse(
                solution.movements().isEmpty());

        assertEquals(
                ChallengeStatus.SOLVED,
                challenge.evaluate(
                        solution.movements()).status());
    }
}