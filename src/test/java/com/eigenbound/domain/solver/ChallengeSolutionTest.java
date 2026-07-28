package com.eigenbound.domain.solver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.eigenbound.domain.math.Vector2;

class ChallengeSolutionTest {

    @Test
    void shouldReportNumberOfMovements() {
        ChallengeSolution solution = new ChallengeSolution(
                List.of(
                        new Vector2(2, 1),
                        new Vector2(1, 2)),
                5);

        assertEquals(2, solution.stepCount());
    }

    @Test
    void shouldProtectMovementsFromModification() {
        List<Vector2> originalMovements = new ArrayList<>();

        originalMovements.add(new Vector2(2, 1));

        ChallengeSolution solution = new ChallengeSolution(
                originalMovements,
                3);

        originalMovements.clear();

        assertEquals(1, solution.movements().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> solution.movements()
                        .add(new Vector2(1, 2)));
    }

    @Test
    void shouldRejectNullMovements() {
        assertThrows(
                NullPointerException.class,
                () -> new ChallengeSolution(null, 0));
    }

    @Test
    void shouldRejectNegativeExploredStates() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ChallengeSolution(
                        List.of(),
                        -1));
    }
}