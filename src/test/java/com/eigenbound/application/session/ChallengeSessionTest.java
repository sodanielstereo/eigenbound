package com.eigenbound.application.session;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

class ChallengeSessionTest {

    private static final Vector2 MOVE_A = new Vector2(2, 1);
    private static final Vector2 MOVE_B = new Vector2(1, 2);

    private ChallengeSession session;

    @BeforeEach
    void setUp() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(5, 4),
                List.of(MOVE_A, MOVE_B),
                3);

        session = new ChallengeSession(challenge);
    }

    @Test
    void shouldBeginAtChallengeStartingPosition() {
        assertEquals(
                new Vector2(0, 0),
                session.currentPosition());
    }

    @Test
    void shouldBeginWithAllStepsAvailable() {
        assertEquals(3, session.remainingSteps());
    }

    @Test
    void shouldSelectAvailableMovement() {
        session.selectMove(MOVE_A);

        assertEquals(
                List.of(MOVE_A),
                session.selectedMoves());
        assertEquals(
                new Vector2(2, 1),
                session.currentPosition());
        assertEquals(2, session.remainingSteps());
    }

    @Test
    void shouldAccumulateSelectedMovements() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        assertEquals(
                new Vector2(3, 3),
                session.currentPosition());
        assertEquals(1, session.remainingSteps());
    }

    @Test
    void shouldRejectUnavailableMovement() {
        Vector2 unavailable = new Vector2(10, 10);

        assertThrows(
                IllegalArgumentException.class,
                () -> session.selectMove(unavailable));
    }

    @Test
    void shouldRejectNullMovement() {
        assertThrows(
                NullPointerException.class,
                () -> session.selectMove(null));
    }

    @Test
    void shouldPreventSelectingMoreThanMaximumSteps() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        assertThrows(
                IllegalStateException.class,
                () -> session.selectMove(MOVE_B));

        assertEquals(3, session.selectedMoves().size());
        assertEquals(0, session.remainingSteps());
    }

    @Test
    void shouldRemoveLastMovementWhenUndoing() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        session.undo();

        assertEquals(
                List.of(MOVE_A),
                session.selectedMoves());
        assertTrue(session.canUndo());
    }

    @Test
    void shouldRestorePositionWhenUndoing() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        session.undo();

        assertEquals(
                new Vector2(2, 1),
                session.currentPosition());
        assertEquals(2, session.remainingSteps());
    }

    @Test
    void undoShouldBeSafeWhenSessionIsEmpty() {
        assertDoesNotThrow(session::undo);

        assertEquals(
                new Vector2(0, 0),
                session.currentPosition());
        assertFalse(session.canUndo());
    }

    @Test
    void shouldResetCurrentAttempt() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        session.reset();

        assertTrue(session.selectedMoves().isEmpty());
        assertEquals(
                new Vector2(0, 0),
                session.currentPosition());
        assertEquals(3, session.remainingSteps());
        assertFalse(session.canUndo());
    }

    @Test
    void shouldReturnSolvedResultForCorrectCombination() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        assertEquals(
                ChallengeStatus.SOLVED,
                session.check().status());
        assertEquals(
                new Vector2(5, 4),
                session.check().finalPosition());
    }

    @Test
    void shouldReturnIncompleteResultForWrongCombination() {
        session.selectMove(MOVE_B);

        assertEquals(
                ChallengeStatus.INCOMPLETE,
                session.check().status());
        assertEquals(
                new Vector2(1, 2),
                session.check().finalPosition());
    }

    @Test
    void selectedMovementListShouldBeImmutable() {
        session.selectMove(MOVE_A);

        List<Vector2> exposedMoves = session.selectedMoves();

        assertThrows(
                UnsupportedOperationException.class,
                () -> exposedMoves.add(MOVE_B));

        assertEquals(
                List.of(MOVE_A),
                session.selectedMoves());
    }

    @Test
    void shouldRejectNullChallenge() {
        assertThrows(
                NullPointerException.class,
                () -> new ChallengeSession(null));
    }
}