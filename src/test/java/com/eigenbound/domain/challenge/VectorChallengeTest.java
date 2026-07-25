package com.eigenbound.domain.challenge;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.math.Vector2;

class VectorChallengeTest {

    private static final Vector2 MOVE_A = new Vector2(2, 1);
    private static final Vector2 MOVE_B = new Vector2(1, 2);

    @Test
    void shouldSolveChallengeWithValidCombination() {
        VectorChallenge challenge = createChallenge();

        ChallengeResult result = challenge.evaluate(
                List.of(MOVE_A, MOVE_A, MOVE_B));

        assertEquals(ChallengeStatus.SOLVED, result.status());
        assertEquals(new Vector2(5, 4), result.finalPosition());
        assertEquals(3, result.stepsUsed());
    }

    @Test
    void shouldReturnIncompleteWhenTargetIsNotReached() {
        VectorChallenge challenge = createChallenge();

        ChallengeResult result = challenge.evaluate(
                List.of(MOVE_A));

        assertEquals(ChallengeStatus.INCOMPLETE, result.status());
        assertEquals(new Vector2(2, 1), result.finalPosition());
        assertEquals(1, result.stepsUsed());
    }

    @Test
    void shouldRejectUnavailableMovement() {
        VectorChallenge challenge = createChallenge();
        Vector2 unavailable = new Vector2(10, 10);

        ChallengeResult result = challenge.evaluate(
                List.of(MOVE_A, unavailable));

        assertEquals(ChallengeStatus.INVALID_MOVE, result.status());
        assertEquals(new Vector2(2, 1), result.finalPosition());
        assertEquals(1, result.stepsUsed());
    }

    @Test
    void shouldRejectNullMovement() {
        VectorChallenge challenge = createChallenge();
        List<Vector2> moves = new ArrayList<>();

        moves.add(MOVE_A);
        moves.add(null);

        ChallengeResult result = challenge.evaluate(moves);

        assertEquals(ChallengeStatus.INVALID_MOVE, result.status());
        assertEquals(new Vector2(2, 1), result.finalPosition());
        assertEquals(1, result.stepsUsed());
    }

    @Test
    void shouldDetectStepLimitExceeded() {
        VectorChallenge challenge = createChallenge();

        ChallengeResult result = challenge.evaluate(
                List.of(MOVE_A, MOVE_A, MOVE_B, MOVE_B));

        assertEquals(
                ChallengeStatus.STEP_LIMIT_EXCEEDED,
                result.status());
        assertEquals(new Vector2(0, 0), result.finalPosition());
        assertEquals(0, result.stepsUsed());
    }

    @Test
    void shouldStartFromNonZeroPosition() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(1, 1),
                new Vector2(3, 2),
                List.of(MOVE_A),
                1);

        ChallengeResult result = challenge.evaluate(
                List.of(MOVE_A));

        assertEquals(ChallengeStatus.SOLVED, result.status());
        assertEquals(new Vector2(3, 2), result.finalPosition());
        assertEquals(1, result.stepsUsed());
    }

    @Test
    void shouldSolveWhenStartAlreadyEqualsTarget() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(2, 2),
                new Vector2(2, 2),
                List.of(MOVE_A),
                3);

        ChallengeResult result = challenge.evaluate(List.of());

        assertEquals(ChallengeStatus.SOLVED, result.status());
        assertEquals(new Vector2(2, 2), result.finalPosition());
        assertEquals(0, result.stepsUsed());
    }

    @Test
    void shouldRejectNullMovementList() {
        VectorChallenge challenge = createChallenge();

        assertThrows(
                NullPointerException.class,
                () -> challenge.evaluate(null));
    }

    @Test
    void shouldRejectNullAvailableMovementList() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorChallenge(
                        new Vector2(0, 0),
                        new Vector2(1, 1),
                        null,
                        3));
    }

    @Test
    void shouldRejectEmptyAvailableMovementList() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorChallenge(
                        new Vector2(0, 0),
                        new Vector2(1, 1),
                        List.of(),
                        3));
    }

    @Test
    void shouldRejectNonPositiveStepLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VectorChallenge(
                        new Vector2(0, 0),
                        new Vector2(1, 1),
                        List.of(MOVE_A),
                        0));
    }

    @Test
    void shouldProtectAvailableMovementsFromModification() {
        List<Vector2> originalMoves = new ArrayList<>();
        originalMoves.add(MOVE_A);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(2, 1),
                originalMoves,
                1);

        originalMoves.add(MOVE_B);

        assertEquals(1, challenge.availableMoves().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> challenge.availableMoves().add(MOVE_B));
    }

    @Test
    void challengeResultShouldRejectNegativeSteps() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ChallengeResult(
                        ChallengeStatus.INCOMPLETE,
                        new Vector2(0, 0),
                        -1));
    }

    private VectorChallenge createChallenge() {
        return new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(5, 4),
                List.of(MOVE_A, MOVE_B),
                3);
    }
}