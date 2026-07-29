package com.eigenbound.application.explanation;

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

class VectorExplanationServiceTest {

    private static final Vector2 MOVE_A = new Vector2(2, 1);

    private static final Vector2 MOVE_B = new Vector2(1, 2);

    private VectorExplanationService service;
    private ChallengeSession session;

    @BeforeEach
    void setUp() {
        service = new VectorExplanationService();

        session = new ChallengeSession(
                new VectorChallenge(
                        new Vector2(0, 0),
                        new Vector2(5, 4),
                        List.of(MOVE_A, MOVE_B),
                        3));
    }

    @Test
    void shouldExplainEverySelectedMovement() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        VectorExplanation explanation = service.explain(session);

        assertEquals(2, explanation.steps().size());

        assertEquals(
                "(0, 0) + (2, 1) = (2, 1)",
                explanation.steps().get(0).equation());

        assertEquals(
                "(2, 1) + (1, 2) = (3, 3)",
                explanation.steps().get(1).equation());
    }

    @Test
    void shouldReportFinalPosition() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        VectorExplanation explanation = service.explain(session);

        assertEquals(
                new Vector2(3, 3),
                explanation.finalPosition());
    }

    @Test
    void shouldExplainSolvedAttempt() {
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_A);
        session.selectMove(MOVE_B);

        VectorExplanation explanation = service.explain(session);

        assertEquals(
                ChallengeStatus.SOLVED,
                explanation.status());

        assertTrue(
                explanation.summary()
                        .contains("alcanzó exactamente"));
    }

    @Test
    void shouldExplainIncompleteAttempt() {
        session.selectMove(MOVE_A);

        VectorExplanation explanation = service.explain(session);

        assertEquals(
                ChallengeStatus.INCOMPLETE,
                explanation.status());

        assertTrue(
                explanation.summary()
                        .contains("todavía no coincide"));
    }

    @Test
    void shouldExplainEmptyAttempt() {
        VectorExplanation explanation = service.explain(session);

        assertTrue(explanation.steps().isEmpty());
        assertEquals(
                new Vector2(0, 0),
                explanation.finalPosition());
    }

    @Test
    void explanationStepsShouldBeImmutable() {
        session.selectMove(MOVE_A);

        VectorExplanation explanation = service.explain(session);

        assertThrows(
                UnsupportedOperationException.class,
                () -> explanation.steps().clear());
    }

    @Test
    void shouldRejectNullSession() {
        assertThrows(
                NullPointerException.class,
                () -> service.explain(null));
    }
}