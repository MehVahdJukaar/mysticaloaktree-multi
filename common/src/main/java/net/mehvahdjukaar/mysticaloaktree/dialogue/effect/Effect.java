package net.mehvahdjukaar.mysticaloaktree.dialogue.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.StatMap;

/**
 * A consequence a dialogue line applies to a player's stats once it plays. Defined and parsed now;
 * because selection happens on the client while stats are server-authoritative, entry effects are
 * not auto-applied yet — that needs a small "I played entry X" packet so the server can apply them
 * from its own copy (anti-cheat). Baseline per-trigger stat changes meanwhile live in the tile.
 */
public interface Effect {

    Codec<Effect> CODEC = Effects.CODEC;

    void apply(StatMap stats);

    MapCodec<? extends Effect> codec();
}
