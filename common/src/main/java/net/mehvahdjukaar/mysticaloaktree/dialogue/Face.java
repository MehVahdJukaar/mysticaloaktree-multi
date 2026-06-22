package net.mehvahdjukaar.mysticaloaktree.dialogue;

import net.minecraft.util.StringRepresentable;

/**
 * Facial expression a dialogue part asks the tree to show. Carried through the model now so
 * the data + selection layers are expression-aware; the visual wiring (block state / overlay)
 * can be hooked up later. QUESTION is meant to signal "he wants an input from you".
 */
public enum Face implements StringRepresentable {
    NEUTRAL("neutral"),
    HAPPY("happy"),
    ANGRY("angry"),
    QUESTION("question"),
    EYEBROW("eyebrow"),
    BLUSH("blush");

    public static final StringRepresentable.EnumCodec<Face> CODEC = StringRepresentable.fromEnum(Face::values);

    private final String name;

    Face(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
