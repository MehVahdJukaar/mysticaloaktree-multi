package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record HeardSound(int trust, String text, GameEvent gameEvent,
                         Optional<RuleTest> blockPredicate,
                         Optional<EntityType<?>> targetEntity) implements ITreeDialogue {

    public static final MapCodec<HeardSound> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text),
                    BuiltInRegistries.GAME_EVENT.byNameCodec().fieldOf("event").forGetter(t -> t.gameEvent),
                    RuleTest.CODEC.optionalFieldOf("block_predicate").forGetter(t -> t.blockPredicate),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("target_entity").forGetter(t -> t.targetEntity)
            ).apply(instance, HeardSound::new));

    @Override
    public Type<HeardSound> getType() {
        return TreeDialogueTypes.HEARD_SOUND;
    }

    @Override
    public int getRequiredTrust() {
        return trust;
    }

    @Override
    public @NotNull Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
        return lineIndex == 0 ? new Status(text) : Status.DONE;
    }
}

