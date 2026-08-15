package com.eigenbound.domain.expedition;

/**
 * Represents the immutable resources carried during an expedition.
 *
 * <p>
 * Stability acts as the run's resilience, while insight is a resource earned
 * from rooms and challenges. Every operation returns a new value instead of
 * mutating the existing instance.
 * </p>
 *
 * @param stability    current stability from zero to the configured maximum
 * @param maxStability maximum stability available during the run
 * @param insight      non-negative insight accumulated by the player
 */
public record ExpeditionResources(
        int stability,
        int maxStability,
        long insight) {

    private static final int INITIAL_MAX_STABILITY = 100;

    /**
     * Validates all resource invariants.
     */
    public ExpeditionResources {
        if (maxStability <= 0) {
            throw new IllegalArgumentException(
                    "Maximum stability must be positive");
        }

        if (stability < 0 || stability > maxStability) {
            throw new IllegalArgumentException(
                    "Stability must be between zero and its maximum");
        }

        if (insight < 0) {
            throw new IllegalArgumentException(
                    "Insight cannot be negative");
        }
    }

    /**
     * Creates the resources carried at the beginning of a new expedition.
     *
     * @return full stability and no accumulated insight
     */
    public static ExpeditionResources initial() {
        return new ExpeditionResources(
                INITIAL_MAX_STABILITY,
                INITIAL_MAX_STABILITY,
                0L);
    }

    /**
     * Reduces stability without allowing it to fall below zero.
     *
     * @param amount stability removed from the run
     * @return updated immutable resources
     */
    public ExpeditionResources loseStability(
            int amount) {
        requireNonNegative(
                amount,
                "Stability loss cannot be negative");

        int updatedStability = (int) Math.max(
                0L,
                (long) stability - amount);

        return new ExpeditionResources(
                updatedStability,
                maxStability,
                insight);
    }

    /**
     * Restores stability without exceeding its configured maximum.
     *
     * @param amount stability restored by an event
     * @return updated immutable resources
     */
    public ExpeditionResources restoreStability(
            int amount) {
        requireNonNegative(
                amount,
                "Stability restoration cannot be negative");

        int updatedStability = (int) Math.min(
                (long) maxStability,
                (long) stability + amount);

        return new ExpeditionResources(
                updatedStability,
                maxStability,
                insight);
    }

    /**
     * Adds insight using an exact operation that rejects numeric overflow.
     *
     * @param amount insight earned by the player
     * @return updated immutable resources
     * @throws ArithmeticException when the result exceeds {@link Long#MAX_VALUE}
     */
    public ExpeditionResources gainInsight(
            long amount) {
        requireNonNegative(
                amount,
                "Insight gain cannot be negative");

        long updatedInsight = Math.addExact(
                insight,
                amount);

        return new ExpeditionResources(
                stability,
                maxStability,
                updatedInsight);
    }

    /**
     * Spends insight when enough of it is available.
     *
     * @param amount insight spent by the player
     * @return updated immutable resources
     */
    public ExpeditionResources spendInsight(
            long amount) {
        requireNonNegative(
                amount,
                "Insight cost cannot be negative");

        if (amount > insight) {
            throw new IllegalArgumentException(
                    "Not enough insight is available");
        }

        return new ExpeditionResources(
                stability,
                maxStability,
                insight - amount);
    }

    /**
     * Indicates whether the run has exhausted all stability.
     *
     * @return {@code true} when stability is zero
     */
    public boolean isDepleted() {
        return stability == 0;
    }

    /**
     * Rejects negative resource adjustments.
     */
    private static void requireNonNegative(
            long amount,
            String message) {
        if (amount < 0) {
            throw new IllegalArgumentException(message);
        }
    }
}