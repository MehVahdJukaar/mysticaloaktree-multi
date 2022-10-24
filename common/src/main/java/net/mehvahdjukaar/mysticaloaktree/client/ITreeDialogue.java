package net.mehvahdjukaar.mysticaloaktree.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ITreeDialogue extends Comparable<ITreeDialogue> {

    Codec<ITreeDialogue> CODEC = StringRepresentable.fromEnum(Type::values).dispatch("type", ITreeDialogue::getType,
            Type::getCodec);

    Type getType();

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

    enum Type implements StringRepresentable {
        NO_OP("no_op", Codec.unit(ITreeDialogue.NO_OP), 100),
        TALKED_WITH("talked_with", TalkedTo.CODEC, 30),
        HEAR_SOUND("hear_sound", HeardSound.CODEC, 20),
        WOKEN_UP("woken_up", WokenUp.CODEC, 50),
        HURT("hurt", Hurt.CODEC, 60);

        private final String name;
        private final int trustDelta;
        private final Codec<? extends ITreeDialogue> codec;

        Type(String name, Codec<? extends ITreeDialogue> codec, int trustDelta) {
            this.name = name;
            this.codec = codec;
            this.trustDelta = trustDelta;
        }

        public Codec<? extends ITreeDialogue> getCodec() {
            return codec;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getTrustDelta( ) {
            return trustDelta;
        }
    }

    ITreeDialogue NO_OP = new Dummy(1000);

    record Dummy(int trust) implements ITreeDialogue {
        @Override
        public Type getType() {
            return Type.NO_OP;
        }

        @Override
        public int getRequiredTrust() {
            return trust;
        }

        @Override
        public Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
            return lineIndex == 0 ? new Status("error") : Status.DONE;
        }
    }

    record Simple(List<String> text) implements ITreeDialogue {

        @Override
        public Type getType() {
            return Type.NO_OP;
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

    record TalkedTo(int trust, List<String> text, List<Boolean> requiresInteraction) implements ITreeDialogue {

        public static final Codec<TalkedTo> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                        Codec.STRING.listOf().fieldOf("text").forGetter(t -> t.text),
                        Codec.BOOL.listOf().optionalFieldOf("require_interaction", List.of()).forGetter(t -> t.requiresInteraction)
                ).apply(instance, TalkedTo::new));

        @Override
        public Type getType() {
            return Type.TALKED_WITH;
        }

        @Override
        public int getRequiredTrust() {
            return trust;
        }

        @Override
        public Status getLine(int lineIndex, boolean hasBeenInteractedWith) {
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

    record WokenUp(int trust, String text) implements ITreeDialogue {

        public static final Codec<WokenUp> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                        Codec.STRING.fieldOf("text").forGetter(t -> t.text)
                ).apply(instance, WokenUp::new));

        @Override
        public Type getType() {
            return Type.WOKEN_UP;
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

    record Hurt(int trust, String text) implements ITreeDialogue {

        public static final Codec<Hurt> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                        Codec.STRING.fieldOf("text").forGetter(t -> t.text)
                ).apply(instance, Hurt::new));

        @Override
        public Type getType() {
            return Type.HURT;
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

    record HeardSound(int trust, String text) implements ITreeDialogue {

        public static final Codec<WokenUp> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                        Codec.STRING.fieldOf("text").forGetter(t -> t.text)
                ).apply(instance, WokenUp::new));

        @Override
        public Type getType() {
            return Type.HEAR_SOUND;
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

}
