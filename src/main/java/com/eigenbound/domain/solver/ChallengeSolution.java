package com.eigenbound.domain.solver;

import java.util.List;
import java.util.Objects;

import com.eigenbound.domain.math.Vector2;

/**
 * Shortest sequence found by the vector-challenge-solving algorithm.
 *
 * @param movements      sequence that reaches the target
 * @param exploredStates number of states examined during the search
 */
public record ChallengeSolution(
        List<Vector2> movements,
        int exploredStates) {

    public ChallengeSolution {
        Objects.requireNonNull(
                movements,
                "Movements cannot be null");

        movements = List.copyOf(movements);

        if (exploredStates < 0) {
            throw new IllegalArgumentException(
                    "Explored states cannot be negative");
        }
    }

    public int stepCount() {
        return movements.size();
    }
}