package net.mehvahdjukaar.mysticaloaktree.dialogue.stat;

/**
 * A single relationship stat the wise oak tracks about a player (or globally).
 * Adding a new stat is intentionally a one-liner in {@link Stats}: storage
 * ({@link StatMap}), serialization and requirements/effects all key off {@link #id()}.
 *
 * @param id           stable serialization id (used in save data, dialogue json and lang)
 * @param min          inclusive lower bound
 * @param max          inclusive upper bound
 * @param defaultValue value assumed when never set (and not stored, to keep save data small)
 */
public record Stat(String id, int min, int max, int defaultValue) {

    public int clamp(int value) {
        return Math.max(min, Math.min(max, value));
    }
}
