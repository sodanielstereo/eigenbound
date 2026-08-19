package com.eigenbound.presentation.expedition;

import java.util.Objects;

import com.eigenbound.application.session.ExpeditionRunState;
import com.eigenbound.application.session.ExpeditionSummary;
import com.eigenbound.domain.expedition.ExpeditionResources;

/**
 * Contains the immutable text and style used by the expedition outcome panel.
 *
 * <p>
 * This view model translates an application-layer summary into user-facing
 * values without introducing JavaFX dependencies. The controller can therefore
 * remain focused on assigning values to controls.
 * </p>
 *
 * @param title            final outcome title
 * @param message          explanation of the final outcome
 * @param seedText         formatted expedition seed
 * @param difficultyText   formatted difficulty
 * @param stabilityText    formatted final stability
 * @param insightText      formatted final insight
 * @param visitedRoomsText formatted number of visited rooms
 * @param styleClass       victory or defeat CSS class
 */
public record ExpeditionOutcomeViewModel(
        String title,
        String message,
        String seedText,
        String difficultyText,
        String stabilityText,
        String insightText,
        String visitedRoomsText,
        String styleClass) {

    /**
     * Validates that every value required by the outcome panel is present.
     */
    public ExpeditionOutcomeViewModel {
        requireText(title, "Outcome title cannot be blank");
        requireText(message, "Outcome message cannot be blank");
        requireText(seedText, "Outcome seed text cannot be blank");
        requireText(difficultyText, "Outcome difficulty text cannot be blank");
        requireText(stabilityText, "Outcome stability text cannot be blank");
        requireText(insightText, "Outcome insight text cannot be blank");
        requireText(visitedRoomsText, "Outcome visited rooms text cannot be blank");
        requireText(styleClass, "Outcome style class cannot be blank");
    }

    /**
     * Creates the presentation values for a terminal expedition summary.
     *
     * @param summary immutable final expedition snapshot
     * @return formatted outcome view model
     */
    public static ExpeditionOutcomeViewModel from(
            ExpeditionSummary summary) {
        Objects.requireNonNull(
                summary,
                "Expedition summary cannot be null");

        String title;
        String message;
        String styleClass;

        if (summary.outcome() == ExpeditionRunState.VICTORY) {
            title = "VICTORIA";
            message = "El núcleo fue estabilizado. La expedición terminó con éxito.";
            styleClass = "outcome-victory";
        } else {
            title = "DERROTA";
            message = "La expedición colapsó al quedarse sin Estabilidad.";
            styleClass = "outcome-defeat";
        }

        ExpeditionResources resources = summary.finalResources();

        return new ExpeditionOutcomeViewModel(
                title,
                message,
                "Semilla: " + summary.seed(),
                "Dificultad: " + summary.difficulty(),
                "Estabilidad final: "
                        + resources.stability()
                        + " / "
                        + resources.maxStability(),
                "Insight final: " + resources.insight(),
                "Habitaciones visitadas: " + summary.visitedRoomCount(),
                styleClass);
    }

    /**
     * Rejects null or blank presentation values.
     */
    private static void requireText(
            String value,
            String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}