package com.eigenbound.domain.puzzle;

import java.util.Objects;

/**
 * Represents one selectable answer in a multiple-choice mini puzzle.
 *
 * @param id   stable identifier used when evaluating an answer
 * @param text user-facing answer text
 */
public record PuzzleOption(
        String id,
        String text) {

    /**
     * Normalizes and validates the option data.
     */
    public PuzzleOption {
        Objects.requireNonNull(
                id,
                "Puzzle option ID cannot be null");

        Objects.requireNonNull(
                text,
                "Puzzle option text cannot be null");

        id = id.trim();
        text = text.trim();

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "Puzzle option ID cannot be blank");
        }

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "Puzzle option text cannot be blank");
        }
    }
}