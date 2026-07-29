package com.eigenbound.domain.expedition.generation;

import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionMap;

/**
 * Contains a procedurally generated expedition and its generation settings.
 *
 * @param map        generated expedition graph
 * @param seed       seed used to generate the graph
 * @param difficulty expedition difficulty from one to five
 */
public record GeneratedExpedition(
        ExpeditionMap map,
        long seed,
        int difficulty) {

    public GeneratedExpedition {
        Objects.requireNonNull(
                map,
                "Expedition map cannot be null");

        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException(
                    "Difficulty must be between one and five");
        }
    }
}