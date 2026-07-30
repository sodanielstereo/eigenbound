package com.eigenbound.domain.expedition.analysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable matrix that describes reachability between graph nodes.
 *
 * <p>
 * A value of {@code true} at position {@code [i][j]} means that the node
 * represented by row {@code i} can reach the node represented by column
 * {@code j}.
 * </p>
 */
public final class ReachabilityMatrix {

    private final List<String> nodeIds;
    private final Map<String, Integer> indexById;
    private final boolean[][] reachable;

    /**
     * Creates an immutable reachability matrix.
     *
     * @param nodeIds   node identifiers in matrix order
     * @param reachable boolean matrix describing reachability between nodes
     */
    public ReachabilityMatrix(
            List<String> nodeIds,
            boolean[][] reachable) {
        Objects.requireNonNull(
                nodeIds,
                "Node IDs cannot be null");
        Objects.requireNonNull(
                reachable,
                "Reachability matrix cannot be null");

        this.nodeIds = normalizeNodeIds(nodeIds);
        validateMatrix(reachable, this.nodeIds.size());

        this.reachable = copyMatrix(reachable);
        this.indexById = buildIndex(this.nodeIds);
    }

    /**
     * Returns the number of nodes represented by the matrix.
     *
     * @return matrix size
     */
    public int size() {
        return nodeIds.size();
    }

    /**
     * Returns node identifiers in their matrix order.
     *
     * @return immutable node identifier list
     */
    public List<String> nodeIds() {
        return nodeIds;
    }

    /**
     * Determines whether one node can reach another.
     *
     * @param sourceId      origin node identifier
     * @param destinationId destination node identifier
     * @return {@code true} when a route exists
     */
    public boolean canReach(
            String sourceId,
            String destinationId) {
        int sourceIndex = requireIndex(sourceId);
        int destinationIndex = requireIndex(destinationId);

        return reachable[sourceIndex][destinationIndex];
    }

    /**
     * Returns every node reachable from a given source.
     *
     * <p>
     * The source is included because every node is considered reachable
     * from itself through a path of length zero.
     * </p>
     *
     * @param sourceId origin node identifier
     * @return immutable list of reachable node IDs
     */
    public List<String> reachableFrom(
            String sourceId) {
        int sourceIndex = requireIndex(sourceId);

        List<String> result = new ArrayList<>();

        for (int destinationIndex = 0; destinationIndex < size(); destinationIndex++) {

            if (reachable[sourceIndex][destinationIndex]) {
                result.add(
                        nodeIds.get(destinationIndex));
            }
        }

        return List.copyOf(result);
    }

    /**
     * Returns a defensive copy of the complete matrix.
     *
     * @return independent boolean matrix
     */
    public boolean[][] matrixCopy() {
        return copyMatrix(reachable);
    }

    /**
     * Normalizes and validates node identifiers.
     */
    private List<String> normalizeNodeIds(
            List<String> originalIds) {
        if (originalIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "Node IDs cannot be empty");
        }

        List<String> normalizedIds = new ArrayList<>();

        Set<String> uniqueIds = new HashSet<>();

        for (String originalId : originalIds) {
            Objects.requireNonNull(
                    originalId,
                    "Node ID cannot be null");

            String normalizedId = originalId.trim();

            if (normalizedId.isBlank()) {
                throw new IllegalArgumentException(
                        "Node ID cannot be blank");
            }

            if (!uniqueIds.add(normalizedId)) {
                throw new IllegalArgumentException(
                        "Duplicate node ID: "
                                + normalizedId);
            }

            normalizedIds.add(normalizedId);
        }

        return List.copyOf(normalizedIds);
    }

    /**
     * Verifies that the supplied matrix is square and matches the node count.
     */
    private void validateMatrix(
            boolean[][] matrix,
            int expectedSize) {
        if (matrix.length != expectedSize) {
            throw new IllegalArgumentException(
                    "Matrix row count must match node count");
        }

        for (boolean[] row : matrix) {
            Objects.requireNonNull(
                    row,
                    "Matrix rows cannot be null");

            if (row.length != expectedSize) {
                throw new IllegalArgumentException(
                        "Reachability matrix must be square");
            }
        }
    }

    /**
     * Creates a node-ID-to-matrix-index lookup.
     */
    private Map<String, Integer> buildIndex(
            List<String> ids) {
        Map<String, Integer> index = new LinkedHashMap<>();

        for (int position = 0; position < ids.size(); position++) {

            index.put(
                    ids.get(position),
                    position);
        }

        return Map.copyOf(index);
    }

    /**
     * Returns the matrix index associated with a node.
     */
    private int requireIndex(String nodeId) {
        Objects.requireNonNull(
                nodeId,
                "Node ID cannot be null");

        String normalizedId = nodeId.trim();

        if (normalizedId.isBlank()) {
            throw new IllegalArgumentException(
                    "Node ID cannot be blank");
        }

        Integer index = indexById.get(normalizedId);

        if (index == null) {
            throw new IllegalArgumentException(
                    "Unknown node ID: "
                            + normalizedId);
        }

        return index;
    }

    /**
     * Creates a deep copy of a two-dimensional boolean matrix.
     */
    private boolean[][] copyMatrix(
            boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];

        for (int row = 0; row < source.length; row++) {

            copy[row] = source[row].clone();
        }

        return copy;
    }
}