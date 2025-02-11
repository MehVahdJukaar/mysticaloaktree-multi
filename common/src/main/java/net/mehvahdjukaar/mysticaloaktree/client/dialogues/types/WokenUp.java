package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import org.jetbrains.annotations.NotNull;

public record WokenUp(int trust, String text) implements ITreeDialogue {

    public static final MapCodec<WokenUp> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text)
            ).apply(instance, WokenUp::new));


    @Override
    public Type<WokenUp> getType() {
        return TreeDialogueTypes.WOKEN_UP;
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
