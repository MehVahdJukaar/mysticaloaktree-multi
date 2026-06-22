package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueContext;

/** Requires another mod to be loaded. Replaces the old top-level {@code "mod_loaded"} hack. */
public record ModLoadedRequirement(String modId) implements Requirement {

    public static final MapCodec<ModLoadedRequirement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ModLoadedRequirement::modId)
    ).apply(instance, ModLoadedRequirement::new));

    @Override
    public boolean test(DialogueContext context) {
        return PlatHelper.isModLoaded(modId);
    }

    @Override
    public MapCodec<? extends Requirement> codec() {
        return CODEC;
    }
}
