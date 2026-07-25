package com.eigenbound.domain.generation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

class GeneratedVectorChallengeTest {

    @Test
    void shouldProtectSolutionFromModification() {
        Vector2 movement = new Vector2(2, 1);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(2, 1),
                List.of(movement),
                1);

        List<Vector2> originalSolution = new ArrayList<>();
        originalSolution.add(movement);

        GeneratedVectorChallenge generated = new GeneratedVectorChallenge(
                challenge,
                originalSolution,
                2026L);

        originalSolution.clear();

        assertEquals(1, generated.solution().size());

        assertThrows(
                UnsupportedOperationException.class,
                () -> generated.solution().add(movement));
    }

    @Test
    void shouldRejectSolutionThatDoesNotSolveChallenge() {
        Vector2 movement = new Vector2(2, 1);

        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(4, 2),
                List.of(movement),
                2);

        assertThrows(
                IllegalArgumentException.class,
                () -> new GeneratedVectorChallenge(
                        challenge,
                        List.of(movement),
                        2026L));
    }

    @Test
    void shouldRejectEmptySolution() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(2, 1),
                List.of(new Vector2(2, 1)),
                1);

        assertThrows(
                IllegalArgumentException.class,
                () -> new GeneratedVectorChallenge(
                        challenge,
                        List.of(),
                        2026L));
    }

    @Test
    void shouldRejectNullChallenge() {
        assertThrows(
                NullPointerException.class,
                () -> new GeneratedVectorChallenge(
                        null,
                        List.of(new Vector2(1, 1)),
                        2026L));
    }

    @Test
    void shouldRejectNullSolution() {
        VectorChallenge challenge = new VectorChallenge(
                new Vector2(0, 0),
                new Vector2(1, 1),
                List.of(new Vector2(1, 1)),
                1);

        assertThrows(
                NullPointerException.class,
                () -> new GeneratedVectorChallenge(
                        challenge,
                        null,
                        2026L));
    }
}