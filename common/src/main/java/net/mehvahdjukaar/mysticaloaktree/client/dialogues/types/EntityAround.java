package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record EntityAround(int trust, String text, Optional<EntityType<?>> targetEntity) implements ITreeDialogue {

    public static final Codec<EntityAround> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text),
                    Registry.ENTITY_TYPE.byNameCodec().optionalFieldOf("target_entity").forGetter(t -> t.targetEntity)
            ).apply(instance, EntityAround::new));

    @Override
    public Type<EntityAround> getType() {
        return TreeDialogueTypes.ENTITY_AROUND;
    }

    @Override
    public int getRequiredTrust() {
        return trust;
    }

    @Override
    public Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
        return lineIndex == 0 ? new Status(text) : Status.DONE;
    }
}

