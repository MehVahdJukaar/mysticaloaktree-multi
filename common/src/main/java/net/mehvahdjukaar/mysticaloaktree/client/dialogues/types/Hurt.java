package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;

public record Hurt(int trust, String text) implements ITreeDialogue {

    public static final Codec<Hurt> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text)
            ).apply(instance, Hurt::new));

    @Override
    public Type<Hurt> getType() {
        return TreeDialogueTypes.HURT;
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