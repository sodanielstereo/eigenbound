package com.eigenbound.domain.solver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import com.eigenbound.domain.challenge.VectorChallenge;
import com.eigenbound.domain.math.Vector2;

/**
 * Finds a shortest solution for a vector challenge using Breadth-First Search.
 *
 * Every movement has the same cost, so breadth-first search guarantees
 * that the first solution found uses the minimum number of movements.
 */
public final class VectorChallengeSolver {

    private static final double EPSILON = 1e-9;

    public Optional<ChallengeSolution> solve(
            VectorChallenge challenge) {
        Objects.requireNonNull(
                challenge,
                "Challenge cannot be null");

        if (approximatelyEquals(
                challenge.start(),
                challenge.target())) {
            return Optional.of(
                    new ChallengeSolution(List.of(), 1));
        }

        Queue<SearchNode> frontier = new ArrayDeque<>();

        Set<Vector2> visited = new HashSet<>();

        SearchNode initialNode = new SearchNode(
                challenge.start(),
                List.of());

        frontier.add(initialNode);
        visited.add(challenge.start());

        int exploredStates = 0;

        while (!frontier.isEmpty()) {
            SearchNode current = frontier.remove();
            exploredStates++;

            if (current.path().size() >= challenge.maxSteps()) {
                continue;
            }

            for (Vector2 movement : challenge.availableMoves()) {

                Vector2 nextPosition = current.position().add(movement);

                List<Vector2> nextPath = appendMovement(
                        current.path(),
                        movement);

                if (approximatelyEquals(
                        nextPosition,
                        challenge.target())) {
                    return Optional.of(
                            new ChallengeSolution(
                                    nextPath,
                                    exploredStates));
                }

                /*
                 * If this position was already discovered, another path
                 * reaching it cannot improve the solution. BFS always
                 * discovers positions using the fewest possible steps first.
                 */
                if (!visited.add(nextPosition)) {
                    continue;
                }

                frontier.add(
                        new SearchNode(
                                nextPosition,
                                nextPath));
            }
        }

        return Optional.empty();
    }

    private List<Vector2> appendMovement(
            List<Vector2> currentPath,
            Vector2 movement) {
        List<Vector2> newPath = new ArrayList<>(currentPath);

        newPath.add(movement);

        return List.copyOf(newPath);
    }

    private boolean approximatelyEquals(
            Vector2 first,
            Vector2 second) {
        return first
                .subtract(second)
                .magnitude() <= EPSILON;
    }

    /**
     * Internal BFS state: current graph vertex and path used to reach it.
     */
    private record SearchNode(
            Vector2 position,
            List<Vector2> path) {

        private SearchNode {
            Objects.requireNonNull(
                    position,
                    "Position cannot be null");
            Objects.requireNonNull(
                    path,
                    "Path cannot be null");

            path = List.copyOf(path);
        }
    }
}