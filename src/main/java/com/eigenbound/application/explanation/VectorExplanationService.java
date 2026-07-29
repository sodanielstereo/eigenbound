package com.eigenbound.application.explanation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.eigenbound.application.session.ChallengeSession;
import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.math.Vector2;

/**
 * Creates step-by-step educational explanations from challenge sessions.
 */
public final class VectorExplanationService {

    /**
     * Explains every vector addition performed during the current attempt.
     *
     * @param session challenge session to explain
     * @return complete educational explanation
     */
    public VectorExplanation explain(
            ChallengeSession session) {
        Objects.requireNonNull(
                session,
                "Session cannot be null");

        List<VectorAdditionStep> steps = createSteps(session);

        ChallengeStatus status = session.check().status();

        return new VectorExplanation(
                steps,
                session.currentPosition(),
                status,
                createSummary(session, status));
    }

    private List<VectorAdditionStep> createSteps(
            ChallengeSession session) {
        List<VectorAdditionStep> steps = new ArrayList<>();

        Vector2 currentPosition = session.challenge().start();

        for (int index = 0; index < session.selectedMoves().size(); index++) {

            Vector2 movement = session.selectedMoves().get(index);

            Vector2 nextPosition = currentPosition.add(movement);

            steps.add(
                    new VectorAdditionStep(
                            index + 1,
                            currentPosition,
                            movement,
                            nextPosition));

            currentPosition = nextPosition;
        }

        return List.copyOf(steps);
    }

    private String createSummary(
            ChallengeSession session,
            ChallengeStatus status) {
        Vector2 displacement = session.currentPosition()
                .subtract(
                        session.challenge().start());

        return switch (status) {
            case SOLVED ->
                "La suma de los vectores produjo el "
                        + "desplazamiento "
                        + format(displacement)
                        + " y alcanzó exactamente el objetivo.";

            case INCOMPLETE ->
                "La suma produjo el desplazamiento "
                        + format(displacement)
                        + ", pero todavía no coincide con "
                        + "el desplazamiento necesario.";

            case INVALID_MOVE ->
                "La secuencia contiene un vector que no "
                        + "pertenece al desafío.";

            case STEP_LIMIT_EXCEEDED ->
                "La secuencia supera la cantidad máxima "
                        + "de movimientos permitidos.";
        };
    }

    private String format(Vector2 vector) {
        return "("
                + formatNumber(vector.x())
                + ", "
                + formatNumber(vector.y())
                + ")";
    }

    private String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }

        return String.format("%.2f", value);
    }
}