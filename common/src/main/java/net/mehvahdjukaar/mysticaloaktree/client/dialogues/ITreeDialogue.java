package net.mehvahdjukaar.mysticaloaktree.client.dialogues;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ITreeDialogue extends Comparable<ITreeDialogue> {

    record Type<T extends ITreeDialogue>(MapCodec<T> codec, String name, int trustDelta) {
    }

    Codec<ITreeDialogue> CODEC = Codec.STRING.<Type<?>>flatXmap(
            (name) -> TreeDialogueTypes.get(name).map(DataResult::success).orElseGet(
                    () -> DataResult.error(()->"Unknown Tree Dialogue type: " + name)),
            (t) -> DataResult.success(t.name()))
            .dispatch("type", ITreeDialogue::getType, Type::codec);

    Type<? extends ITreeDialogue> getType();

    int getRequiredTrust();

    @Override
    default int compareTo(@NotNull ITreeDialogue o) {
        return Integer.compare(this.getRequiredTrust(), o.getRequiredTrust());
    }

    default DialogueInstance createInstance() {
        return new DialogueInstance(this);
    }

    @NotNull
    ITreeDialogue.Status getLine(int lineIndex, boolean hasBeenInteractedWith);

    record Status(String text) {
        public static Status DONE = new Status(null);
        public static Status WAITING = new Status(null);
    }


    ITreeDialogue NO_OP = new Dummy(1000);

    record Dummy(int trust) implements ITreeDialogue {
        @Override
        public Type<Dummy> getType() {
            return null;
        }

        @Override
        public int getRequiredTrust() {
            return trust;
        }

        @Override
        public @NotNull Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
            return lineIndex == 0 ? new Status("error") : Status.DONE;
        }
    }

    record Simple(List<String> text) implements ITreeDialogue {

        @Override
        public Type<ITreeDialogue> getType() {
            return null;
        }

        @Override
        public int getRequiredTrust() {
            return 0;
        }

        @Override
        public @NotNull Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
            if (lineIndex < text.size()) {
                String s = text.get(lineIndex);
                return new Status(s);
            }
            return Status.DONE;
        }
    }

}
