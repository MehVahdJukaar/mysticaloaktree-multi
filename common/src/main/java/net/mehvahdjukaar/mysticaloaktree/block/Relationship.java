package net.mehvahdjukaar.mysticaloaktree.block;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.StatMap;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stats;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * A player's standing with one wise oak. Now a thin layer over a {@link StatMap} so new stats
 * (familiarity, romance, fatigue, ...) are tracked and persisted automatically; the trust-based
 * helpers are kept for the existing tile behavior.
 */
public class Relationship {

    private static final int TIME_BETWEEN_CONVERSATIONS = 3 * 18;
    private static final int HURT_DECREMENT = 20;
    private static final int CONVERSATION_INCREMENT = 5;
    private static final int FAMILIARITY_INCREMENT = 1;

    /** Per-player value is either the new stat map or, for old saves, a bare trust int. */
    public static final Codec<Relationship> CODEC = Codec.either(StatMap.CODEC, Codec.INT)
            .xmap(either -> either.map(Relationship::new, Relationship::ofTrust),
                    r -> com.mojang.datafixers.util.Either.left(r.stats));

    private final StatMap stats;
    private long lastInteractedTimeStamp;

    public Relationship(StatMap stats) {
        this.stats = stats;
        this.lastInteractedTimeStamp = 0;
    }

    public static Relationship ofTrust(int trust) {
        StatMap map = new StatMap();
        map.set(Stats.TRUST, trust);
        return new Relationship(map);
    }

    public StatMap getStats() {
        return stats;
    }

    public boolean checkTalkCooldown(Level level) {
        long time = level.getGameTime();
        if (Math.abs(time - lastInteractedTimeStamp) > TIME_BETWEEN_CONVERSATIONS) {
            lastInteractedTimeStamp = time;
            stats.add(Stats.TRUST, CONVERSATION_INCREMENT);
            stats.add(Stats.FAMILIARITY, FAMILIARITY_INCREMENT);
            return true;
        }
        return false;
    }

    public void decrease() {
        stats.add(Stats.TRUST, -HURT_DECREMENT);
    }

    public int getTrust() {
        return stats.get(Stats.TRUST);
    }

    //blows away
    public boolean isAngry() {
        return getTrust() < 0;
    }

    //rotates at start of conversation
    public boolean isFriendlyAt() {
        return getTrust() > 40;
    }

    //keeps track during conversation
    public boolean isInConfidence() {
        return getTrust() > 70;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Relationship that)) return false;
        return getTrust() == that.getTrust();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getTrust());
    }
}
