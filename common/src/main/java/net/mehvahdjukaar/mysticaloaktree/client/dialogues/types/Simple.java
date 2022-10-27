package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;

import java.util.List;

record Simple(List<String> text) implements ITreeDialogue {

    @Override
    public Type<Simple> getType() {
        return ITreeDialogue.NO_OP;
    }

    @Override
    public int getRequiredTrust() {
        return 0;
    }

    @Override
    public Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
        if (lineIndex < text.size()) {
            String s = text.get(lineIndex);
            return new Status(s);
        }
        return Status.DONE;
    }
}

