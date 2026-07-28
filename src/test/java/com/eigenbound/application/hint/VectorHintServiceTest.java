package com.eigenbound.application.hint;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eigenbound.application.session.ChallengeSession;
import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;
import com.eigenbound.domain.solver.VectorChallengeSolver;

class VectorHintServiceTest {

    private static final Vector2 MOVE_A = new Vector2(2, 1);

    private static final Vector2 MOVE_B = new Vector2(1, 2);

    private VectorHintService service;
    private ChallengeSession session;

    @BeforeEach
    void setUp() {
        service = new VectorHintService();

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(5, 4),
                List.of(MOVE_A, MOVE_B),
                3);

        session = new ChallengeSession(challenge);
    }

    @Test
    void shouldCreateConceptualHint() {
        VectorHint hint = service.generateHint(
                session,
                HintLevel.CONCEPTUAL);

        assertEquals(
                HintLevel.CONCEPTUAL,
                hint.level());
        assertTrue(hint.movements().isEmpty());
        assertTrue(
                hint.message().contains("sumando"));
    }

    @Test
    void shouldShowRemainingDisplacement() {
        VectorHint hint = service.generateHint(
                session,
                HintLevel.DIRECTIONAL);

        assertTrue(
                hint.message().contains("(5, 4)"));
        assertTrue(hint.movements().isEmpty());
    }

    @Test
    void directionalHintShouldUseCurrentPosition() {
        session.selectMove(MOVE_A);

        VectorHint hint = service.generateHint(
                session,
                HintLevel.DIRECTIONAL);

        assertTrue(
                hint.message().contains("(3, 3)"));
    }

    @Test
    void shouldSuggestNextMovementUsingBfs() {
        VectorHint hint = service.generateHint(
                session,
                HintLevel.NEXT_MOVE);

        assertEquals(1, hint.movements().size());
        assertTrue(
                session.challenge()
                        .availableMoves()
                        .contains(
                                hint.movements().get(0)));
    }

    @Test
    void shouldReturnCompleteShortestSolution() {
        VectorHint hint = service.generateHint(
                session,
                HintLevel.FULL_SOLUTION);

        assertEquals(3, hint.movements().size());

        assertEquals(
                ChallengeStatus.SOLVED,
                session.challenge()
                        .evaluate(hint.movements())
                        .status());
    }

    @Test
    void shouldSolveFromCurrentPosition() {
        session.selectMove(MOVE_A);

        VectorHint hint = service.generateHint(
                session,
                HintLevel.FULL_SOLUTION);

        assertEquals(2, hint.movements().size());

        session.selectMove(hint.movements().get(0));
        session.selectMove(hint.movements().get(1));

        assertEquals(
                ChallengeStatus.SOLVED,
                session.check().status());
    }

    @Test
    void shouldNotSuggestMovesForSolvedSession() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        VectorHint hint = service.generateHint(
                session,
                HintLevel.NEXT_MOVE);

        assertTrue(hint.movements().isEmpty());
        assertTrue(
                hint.message().contains(
                        "Ya alcanzaste"));
    }

    @Test
    void shouldReportWhenNoStepsRemain() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(10, 10),
                List.of(new Vector2(1, 0)),
                1);

        ChallengeSession limitedSession = new ChallengeSession(challenge);

        limitedSession.selectMove(
                new Vector2(1, 0));

        VectorHint hint = service.generateHint(
                limitedSession,
                HintLevel.NEXT_MOVE);

        assertTrue(hint.movements().isEmpty());
        assertTrue(
                hint.message().contains(
                        "No existe una solución"));
    }

    @Test
    void shouldReportImpossibleRemainingChallenge() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(1, 0),
                List.of(new Vector2(2, 0)),
                3);

        ChallengeSession impossibleSession = new ChallengeSession(challenge);

        VectorHint hint = service.generateHint(
                impossibleSession,
                HintLevel.FULL_SOLUTION);

        assertTrue(hint.movements().isEmpty());
        assertTrue(
                hint.message().contains(
                        "No existe una solución"));
    }

    @Test
    void shouldRejectNullSession() {
        assertThrows(
                NullPointerException.class,
                () -> service.generateHint(
                        null,
                        HintLevel.CONCEPTUAL));
    }

    @Test
    void shouldRejectNullLevel() {
        assertThrows(
                NullPointerException.class,
                () -> service.generateHint(
                        session,
                        null));
    }

    @Test
    void shouldRejectNullSolver() {
        assertThrows(
                NullPointerException.class,
                () -> new VectorHintService(
                        (VectorChallengeSolver) null));
    }
}