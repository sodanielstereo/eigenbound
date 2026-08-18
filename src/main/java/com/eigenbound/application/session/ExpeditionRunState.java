package com.eigenbound.application.session;

/**
 * Represents the lifecycle state of an expedition run.
 *
 * <p>
 * A run starts active and reaches exactly one terminal state: victory after
 * reaching the boss room or defeat after exhausting all stability.
 * </p>
 */
public enum ExpeditionRunState {

    /** The player can still select rooms and change resources. */
    ACTIVE(false),

    /** The player reached and completed the expedition boss room. */
    VICTORY(true),

    /** The expedition exhausted all of its stability. */
    DEFEAT(true);

    private final boolean terminal;

    ExpeditionRunState(
            boolean terminal) {
        this.terminal = terminal;
    }

    /**
     * Indicates whether the run can no longer change.
     *
     * @return {@code true} for victory and defeat
     */
    public boolean isTerminal() {
        return terminal;
    }
}