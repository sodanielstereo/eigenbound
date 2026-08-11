package com.eigenbound.domain.puzzle;

/**
 * Represents the terminal outcome of a mini-puzzle attempt.
 */
public enum PuzzleStatus {

    /** The player selected the correct option before time expired. */
    SOLVED,

    /** The player selected an incorrect option before time expired. */
    INCORRECT,

    /** The puzzle time limit expired before an answer was accepted. */
    TIMED_OUT,

    /** The attempt ended without evaluating an answer. */
    CANCELLED;

    /**
     * Indicates whether this outcome must reference a selected option.
     *
     * @return {@code true} for outcomes produced by evaluating an answer
     */
    public boolean requiresSelectedOption() {
        return this == SOLVED
                || this == INCORRECT;
    }
}