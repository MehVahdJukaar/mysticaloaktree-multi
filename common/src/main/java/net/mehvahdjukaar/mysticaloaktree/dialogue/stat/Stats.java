package net.mehvahdjukaar.mysticaloaktree.dialogue.stat;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry of all relationship stats. To add a stat (e.g. a new "mood"), add one
 * {@code register(...)} line here — every other system (save data, json requirements,
 * effects) picks it up automatically by id.
 */
public final class Stats {

    private static final Map<String, Stat> REGISTRY = new LinkedHashMap<>();

    /** Anger/affection toward the player. Can go negative (he blows you away when angry). */
    public static final Stat TRUST = register(new Stat("trust", -20, 100, 0));
    /** Whether he knows you. Grows over time, decays only very slowly ("treementia"). */
    public static final Stat FAMILIARITY = register(new Stat("familiarity", 0, 100, 0));
    /** Romantic affection. Decays the fastest of all stats. */
    public static final Stat ROMANCE = register(new Stat("romance", 0, 100, 0));
    /** How tired he is. Gates how many lines he tells before sleeping. */
    public static final Stat FATIGUE = register(new Stat("fatigue", 0, 100, 0));

    private Stats() {
    }

    public static Stat register(Stat stat) {
        if (REGISTRY.containsKey(stat.id())) {
            throw new IllegalStateException("Duplicate stat id: " + stat.id());
        }
        REGISTRY.put(stat.id(), stat);
        return stat;
    }

    @Nullable
    public static Stat get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<Stat> all() {
        return REGISTRY.values();
    }

    /** Forces class load so the static registrations run. */
    public static void init() {
    }
}
