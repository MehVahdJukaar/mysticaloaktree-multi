package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;

public record OnBroken(int trust, String text, boolean silkTouch) implements ITreeDialogue {

    public static final Codec<OnBroken> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text),
                    Codec.BOOL.fieldOf("silk_touch").forGetter(t->t.silkTouch)
            ).apply(instance, OnBroken::new));

    @Override
    public Type<OnBroken> getType() {
        return TreeDialogueTypes.ON_BROKEN;
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

