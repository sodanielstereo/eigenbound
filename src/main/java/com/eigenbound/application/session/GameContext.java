package com.eigenbound.application.session;

import java.util.Objects;
import java.util.Optional;

import com.eigenbound.domain.expedition.generation.GeneratedExpedition;

/**
 * Stores application state that must survive JavaFX view changes.
 *
 * <p>
 * FXML navigation creates a new controller whenever a view is loaded. This
 * context keeps the active expedition run outside those controllers so the
 * same progress can be reused by the map and the challenge laboratory.
 * </p>
 */
public final class GameContext {

    private ExpeditionRun activeExpeditionRun;

    /**
     * Creates and stores a new expedition run.
     *
     * @param generatedExpedition generated expedition used by the new run
     * @return newly created expedition run
     */
    public ExpeditionRun startExpedition(
            GeneratedExpedition generatedExpedition) {

        activeExpeditionRun = new ExpeditionRun(
                Objects.requireNonNull(
                        generatedExpedition,
                        "Generated expedition cannot be null"));

        return activeExpeditionRun;
    }

    /**
     * Returns the active expedition when one has been started.
     *
     * @return optional active expedition run
     */
    public Optional<ExpeditionRun> activeExpeditionRun() {
        return Optional.ofNullable(activeExpeditionRun);
    }

    /**
     * Indicates whether the application contains an active expedition.
     *
     * @return true when an expedition run is active
     */
    public boolean hasActiveExpeditionRun() {
        return activeExpeditionRun != null;
    }

    /**
     * Returns the active expedition or rejects access when none exists.
     *
     * @return active expedition run
     * @throws IllegalStateException when no expedition has been started
     */
    public ExpeditionRun requireActiveExpeditionRun() {
        if (activeExpeditionRun == null) {
            throw new IllegalStateException(
                    "No expedition run is currently active");
        }

        return activeExpeditionRun;
    }

    /**
     * Removes the active expedition from the application context.
     */
    public void clearExpeditionRun() {
        activeExpeditionRun = null;
    }
}