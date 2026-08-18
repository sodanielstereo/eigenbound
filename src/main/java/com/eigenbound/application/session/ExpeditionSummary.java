package com.eigenbound.application.session;

import java.util.Objects;

import com.eigenbound.domain.expedition.ExpeditionResources;

/**
 * Captures the immutable final result of one expedition run.
 *
 * <p>
 * A summary can only represent a terminal run. It preserves the generation
 * settings, final resources and number of visited rooms so presentation code
 * can render an outcome without mutating or recalculating expedition state.
 * </p>
 *
 * @param outcome          final victory or defeat state
 * @param seed             seed used to generate the expedition
 * @param difficulty       expedition difficulty from one to five
 * @param finalResources   immutable resources at the end of the run
 * @param visitedRoomCount number of rooms included in the traversed path
 */
public record ExpeditionSummary(
        ExpeditionRunState outcome,
        long seed,
        int difficulty,
        ExpeditionResources finalResources,
        int visitedRoomCount) {

    /**
     * Validates that the snapshot represents a consistent terminal run.
     */
    public ExpeditionSummary {
        Objects.requireNonNull(
                outcome,
                "Expedition outcome cannot be null");
        Objects.requireNonNull(
                finalResources,
                "Final expedition resources cannot be null");

        if (!outcome.isTerminal()) {
            throw new IllegalArgumentException(
                    "Expedition summary requires a terminal outcome");
        }

        if (difficulty < 1 || difficulty > 5) {
            throw new IllegalArgumentException(
                    "Difficulty must be between one and five");
        }

        if (visitedRoomCount < 1) {
            throw new IllegalArgumentException(
                    "Visited room count must be positive");
        }

        if (outcome == ExpeditionRunState.VICTORY
                && finalResources.isDepleted()) {
            throw new IllegalArgumentException(
                    "Victory requires remaining stability");
        }

        if (outcome == ExpeditionRunState.DEFEAT
                && !finalResources.isDepleted()) {
            throw new IllegalArgumentException(
                    "Defeat requires depleted stability");
        }
    }
}