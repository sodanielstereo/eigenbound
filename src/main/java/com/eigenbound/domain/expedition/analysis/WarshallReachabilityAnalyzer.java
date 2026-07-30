package com.eigenbound.domain.expedition.analysis;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionEdge;
import com.eigenbound.domain.expedition.ExpeditionMap;
import com.eigenbound.domain.expedition.ExpeditionNode;

/**
 * Calculates the transitive closure of an expedition graph using Warshall's
 * algorithm.
 *
 * <p>
 * The resulting matrix answers whether any node can reach any other node,
 * directly or through intermediate rooms.
 * </p>
 */
public final class WarshallReachabilityAnalyzer {

    /**
     * Calculates all-pairs reachability for an expedition.
     *
     * @param expeditionMap graph to analyze
     * @return immutable transitive-closure matrix
     */
    public ReachabilityMatrix analyze(
            ExpeditionMap expeditionMap) {
        Objects.requireNonNull(
                expeditionMap,
                "Expedition map cannot be null");

        List<ExpeditionNode> nodes = expeditionMap.nodes();

        Map<String, Integer> indexById = buildIndex(nodes);

        boolean[][] reachable = createInitialMatrix(
                nodes,
                expeditionMap.edges(),
                indexById);

        applyWarshall(reachable);

        List<String> nodeIds = nodes.stream()
                .map(ExpeditionNode::id)
                .toList();

        return new ReachabilityMatrix(
                nodeIds,
                reachable);
    }

    /**
     * Assigns one matrix index to each graph node.
     */
    private Map<String, Integer> buildIndex(
            List<ExpeditionNode> nodes) {
        Map<String, Integer> indexById = new LinkedHashMap<>();

        for (int index = 0; index < nodes.size(); index++) {

            indexById.put(
                    nodes.get(index).id(),
                    index);
        }

        return Map.copyOf(indexById);
    }

    /**
     * Creates the initial matrix based in adjacency.
     * 
     * <p>
     * Every node initially reaches itself, and every direct edge marks its
     * source-to-destination position as reachable.
     * </p>
     */
    private boolean[][] createInitialMatrix(
            List<ExpeditionNode> nodes,
            List<ExpeditionEdge> edges,
            Map<String, Integer> indexById) {
        int size = nodes.size();

        boolean[][] reachable = new boolean[size][size];

        for (int index = 0; index < size; index++) {

            reachable[index][index] = true;
        }

        for (ExpeditionEdge edge : edges) {
            int sourceIndex = indexById.get(edge.sourceId());

            int destinationIndex = indexById.get(
                    edge.destinationId());

            reachable[sourceIndex][destinationIndex] = true;
        }

        return reachable;
    }

    /**
     * Applies Warshall's dynamic-programming recurrence.
     *
     * <p>
     * After processing intermediate node {@code k}, the matrix knows every
     * route that can be formed using nodes from zero through {@code k} as
     * intermediate points.
     * </p>
     */
    private void applyWarshall(
            boolean[][] reachable) {
        int size = reachable.length;

        for (int intermediate = 0; intermediate < size; intermediate++) {

            for (int source = 0; source < size; source++) {

                /*
                 * If the source cannot reach the current intermediate node,
                 * that intermediate node cannot create a new route.
                 */
                if (!reachable[source][intermediate]) {
                    continue;
                }

                for (int destination = 0; destination < size; destination++) {

                    reachable[source][destination] = reachable[source][destination]
                            || (reachable[source][intermediate]
                                    && reachable[intermediate][destination]);
                }
            }
        }
    }
}