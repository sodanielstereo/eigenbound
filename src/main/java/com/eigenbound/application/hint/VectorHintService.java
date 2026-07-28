package com.eigenbound.application.hint;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.eigenbound.application.session.ChallengeSession;
import com.eigenbound.domain.challenge.ChallengeStatus;
import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;
import com.eigenbound.domain.solver.ChallengeSolution;
import com.eigenbound.domain.solver.VectorChallengeSolver;

public final class VectorHintService {

    private final VectorChallengeSolver solver;

    public VectorHintService() {
        this(new VectorChallengeSolver());
    }

    public VectorHintService(
            VectorChallengeSolver solver) {
        this.solver = Objects.requireNonNull(
                solver,
                "Solver cannot be null");
    }

    public VectorHint generateHint(
            ChallengeSession session,
            HintLevel level) {
        Objects.requireNonNull(
                session,
                "Session cannot be null");
        Objects.requireNonNull(
                level,
                "Hint level cannot be null");

        if (session.check().status() == ChallengeStatus.SOLVED) {
            return new VectorHint(
                    level,
                    "Ya alcanzaste el objetivo. "
                            + "No necesitas más movimientos.",
                    List.of());
        }

        return switch (level) {
            case CONCEPTUAL ->
                createConceptualHint();

            case DIRECTIONAL ->
                createDirectionalHint(session);

            case NEXT_MOVE ->
                createNextMoveHint(session);

            case FULL_SOLUTION ->
                createFullSolutionHint(session);
        };
    }

    private VectorHint createConceptualHint() {
        return new VectorHint(
                HintLevel.CONCEPTUAL,
                "Piensa en cada vector como un desplazamiento. "
                        + "La posición final se obtiene sumando "
                        + "los vectores seleccionados.",
                List.of());
    }

    private VectorHint createDirectionalHint(
            ChallengeSession session) {
        Vector2 displacement = session.challenge()
                .target()
                .subtract(
                        session.currentPosition());

        String message = "Desde tu posición actual todavía necesitas "
                + "un desplazamiento total de "
                + formatVector(displacement)
                + ".";

        return new VectorHint(
                HintLevel.DIRECTIONAL,
                message,
                List.of());
    }

    private VectorHint createNextMoveHint(
            ChallengeSession session) {
        Optional<ChallengeSolution> solution = solveRemainingChallenge(session);

        if (solution.isEmpty()) {
            return noSolutionHint(
                    HintLevel.NEXT_MOVE);
        }

        Vector2 nextMovement = solution.orElseThrow()
                .movements()
                .get(0);

        return new VectorHint(
                HintLevel.NEXT_MOVE,
                "Un posible siguiente movimiento es "
                        + formatVector(nextMovement)
                        + ".",
                List.of(nextMovement));
    }

    private VectorHint createFullSolutionHint(
            ChallengeSession session) {
        Optional<ChallengeSolution> solution = solveRemainingChallenge(session);

        if (solution.isEmpty()) {
            return noSolutionHint(
                    HintLevel.FULL_SOLUTION);
        }

        ChallengeSolution challengeSolution = solution.orElseThrow();

        return new VectorHint(
                HintLevel.FULL_SOLUTION,
                "Una solución mínima desde tu posición "
                        + "actual utiliza "
                        + challengeSolution.stepCount()
                        + " movimiento(s): "
                        + formatMovements(
                                challengeSolution.movements())
                        + ".",
                challengeSolution.movements());
    }

    private Optional<ChallengeSolution> solveRemainingChallenge(
            ChallengeSession session) {

        if (session.remainingSteps() <= 0) {
            return Optional.empty();
        }

        VectorChallenge remainingChallenge = new VectorChallenge(
                session.currentPosition(),
                session.challenge().target(),
                session.challenge()
                        .availableMoves(),
                session.remainingSteps());

        return solver.solve(remainingChallenge);
    }

    private VectorHint noSolutionHint(
            HintLevel level) {
        return new VectorHint(
                level,
                "No existe una solución desde la posición "
                        + "actual con los movimientos restantes. "
                        + "Prueba deshacer o reiniciar.",
                List.of());
    }

    private String formatMovements(
            List<Vector2> movements) {
        return movements.stream()
                .map(this::formatVector)
                .reduce(
                        (first, second) -> first + " + " + second)
                .orElse("ninguno");
    }

    private String formatVector(Vector2 vector) {
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