package com.eigenbound.application.effect;

/**
 * Describes the immutable resource changes produced by an expedition room.
 *
 * <p>
 * Stability restoration and stability loss are stored separately instead of
 * using a signed delta. This keeps every amount non-negative and avoids edge
 * cases such as negating {@link Integer#MIN_VALUE}. An effect may also grant
 * insight, which is represented as a {@code long} to match the expedition
 * resource model.
 * </p>
 *
 * @param stabilityRestored amount of stability restored by the room
 * @param stabilityLost     amount of stability removed by the room
 * @param insightGained     amount of insight granted by the room
 */
public record RoomEffect(
        int stabilityRestored,
        int stabilityLost,
        long insightGained) {

    private static final RoomEffect NONE = new RoomEffect(
            0,
            0,
            0L);

    /**
     * Validates that the effect contains only safe resource adjustments.
     */
    public RoomEffect {
        if (stabilityRestored < 0) {
            throw new IllegalArgumentException(
                    "Stability restoration cannot be negative");
        }

        if (stabilityLost < 0) {
            throw new IllegalArgumentException(
                    "Stability loss cannot be negative");
        }

        if (insightGained < 0) {
            throw new IllegalArgumentException(
                    "Insight gain cannot be negative");
        }

        if (stabilityRestored > 0
                && stabilityLost > 0) {
            throw new IllegalArgumentException(
                    "An effect cannot restore and remove stability together");
        }
    }

    /**
     * Creates an effect that does not change expedition resources.
     *
     * @return shared empty effect
     */
    public static RoomEffect none() {
        return NONE;
    }

    /**
     * Creates an effect that restores expedition stability.
     *
     * @param amount positive amount of stability restored
     * @return stability restoration effect
     */
    public static RoomEffect restoreStability(
            int amount) {
        requirePositive(
                amount,
                "Stability restoration must be positive");

        return new RoomEffect(
                amount,
                0,
                0L);
    }

    /**
     * Creates an effect that removes expedition stability.
     *
     * @param amount positive amount of stability removed
     * @return stability loss effect
     */
    public static RoomEffect loseStability(
            int amount) {
        requirePositive(
                amount,
                "Stability loss must be positive");

        return new RoomEffect(
                0,
                amount,
                0L);
    }

    /**
     * Creates an effect that grants expedition insight.
     *
     * @param amount positive amount of insight granted
     * @return insight reward effect
     */
    public static RoomEffect gainInsight(
            long amount) {
        requirePositive(
                amount,
                "Insight gain must be positive");

        return new RoomEffect(
                0,
                0,
                amount);
    }

    /**
     * Indicates whether at least one expedition resource will change.
     *
     * @return {@code true} when this is not an empty effect
     */
    public boolean changesResources() {
        return stabilityRestored > 0
                || stabilityLost > 0
                || insightGained > 0;
    }

    /**
     * Rejects zero and negative adjustments created through named factories.
     */
    private static void requirePositive(
            long amount,
            String message) {
        if (amount <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}