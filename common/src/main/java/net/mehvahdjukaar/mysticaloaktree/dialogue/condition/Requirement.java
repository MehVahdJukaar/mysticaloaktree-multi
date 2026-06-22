package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;

/**
 * A data-driven predicate gating whether a dialogue line is eligible in a given
 * {@link DialogueContext}. New condition kinds are added by implementing this and registering a
 * {@link MapCodec} in {@link Requirements} — no changes to selection or the entry codec needed.
 */
public interface Requirement {

    Codec<Requirement> CODEC = Requirements.CODEC;

    boolean test(DialogueContext context);

    MapCodec<? extends Requirement> codec();
}
