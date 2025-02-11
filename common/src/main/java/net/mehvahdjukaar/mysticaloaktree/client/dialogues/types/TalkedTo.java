package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;


import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record TalkedTo(int trust, List<String> text, List<Boolean> requiresInteraction) implements ITreeDialogue {

    public static final MapCodec<TalkedTo> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.listOf().fieldOf("text").forGetter(t -> t.text),
                    Codec.BOOL.listOf().optionalFieldOf("requires_interactions", List.of()).forGetter(t -> t.requiresInteraction)
            ).apply(instance, TalkedTo::new));

    @Override
    public Type<TalkedTo> getType() {
        return TreeDialogueTypes.TALKED_TO;
    }

    @Override
    public int getRequiredTrust() {
        return trust;
    }

    @Override
    public @NotNull Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
        if (lineIndex < text.size()) {
            String s = text.get(lineIndex);
            if (lineIndex > 0 && lineIndex - 1 < requiresInteraction.size()) {
                if (requiresInteraction.get(lineIndex - 1) && !hasBeenInteractedWith) {
                    return Status.WAITING;
                }
            }
            return new Status(s);
        }
        return Status.DONE;
    }
}
