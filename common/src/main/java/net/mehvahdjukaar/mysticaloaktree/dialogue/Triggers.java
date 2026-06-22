package net.mehvahdjukaar.mysticaloaktree.dialogue;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/** Registry of dialogue triggers. */
public final class Triggers {

    private static final Map<String, Trigger> REGISTRY = new LinkedHashMap<>();

    public static final Trigger TALKED_TO = register(new Trigger("talked_to"));
    public static final Trigger WOKEN_UP = register(new Trigger("woken_up"));
    public static final Trigger HURT = register(new Trigger("hurt"));
    public static final Trigger PROJECTILE_HIT = register(new Trigger("projectile_hit"));
    public static final Trigger ENTITY_AROUND = register(new Trigger("entity_around"));
    public static final Trigger HEARD_SOUND = register(new Trigger("heard_sound"));
    public static final Trigger ON_BROKEN = register(new Trigger("on_broken"));
    // new triggers from the rework brainstorm, not wired yet
    public static final Trigger ITEM_GIVEN = register(new Trigger("item_given"));
    public static final Trigger BLOCK_PLACED = register(new Trigger("block_placed"));

    private Triggers() {
    }

    public static Trigger register(Trigger trigger) {
        REGISTRY.put(trigger.id(), trigger);
        return trigger;
    }

    @Nullable
    public static Trigger get(String id) {
        return REGISTRY.get(id);
    }

    public static void init() {
    }
}
