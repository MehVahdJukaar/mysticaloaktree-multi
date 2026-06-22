package net.mehvahdjukaar.mysticaloaktree.dialogue.stat;

import com.mojang.serialization.Codec;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A mutable bag of stat values keyed by {@link Stat}. Values are clamped to each stat's
 * range and defaults are never stored, so the serialized form stays small and forward
 * compatible: unknown stat ids (renamed/removed) are silently dropped on load.
 */
public final class StatMap {

    private final Map<Stat, Integer> values = new HashMap<>();

    public StatMap() {
    }

    public int get(Stat stat) {
        return values.getOrDefault(stat, stat.defaultValue());
    }

    public void set(Stat stat, int value) {
        values.put(stat, stat.clamp(value));
    }

    public void add(Stat stat, int delta) {
        set(stat, get(stat) + delta);
    }

    public StatMap copy() {
        StatMap c = new StatMap();
        c.values.putAll(this.values);
        return c;
    }

    // ---- serialization ----

    private Map<String, Integer> serialize() {
        Map<String, Integer> out = new HashMap<>();
        values.forEach((stat, v) -> {
            if (v != stat.defaultValue()) out.put(stat.id(), v);
        });
        return out;
    }

    private static StatMap deserialize(Map<String, Integer> raw) {
        StatMap map = new StatMap();
        raw.forEach((id, v) -> {
            Stat stat = Stats.get(id);
            if (stat != null) map.set(stat, v);
        });
        return map;
    }

    public static final Codec<StatMap> CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT)
            .xmap(StatMap::deserialize, StatMap::serialize);
}
