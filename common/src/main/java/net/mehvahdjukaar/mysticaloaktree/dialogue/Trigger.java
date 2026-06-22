package net.mehvahdjukaar.mysticaloaktree.dialogue;

/**
 * The reason a dialogue fires (talked to, hurt, woken up, ...). A dialogue entry references
 * its trigger by {@link #id()}; the same id is used in the dialogue json {@code "trigger"} field
 * (and the legacy {@code "type"} field).
 */
public record Trigger(String id) {
}
