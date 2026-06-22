package net.mehvahdjukaar.mysticaloaktree.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.api.util.codec.CodecUtils;

/**
 * One streamed line of a dialogue. {@link #text} is the inline default (doubles as en_us); a lang
 * file may override it by id (see {@link DialogueEntry#langKey}). In the {@code "lines"} array a part
 * may be written either as a bare string or as a full object {@code {"text":..,"face":..}}.
 *
 * @param requiresInteraction if true, the tree waits for the player to click before showing this part
 */
public record DialoguePart(String text, Face face, boolean requiresInteraction) {

    private static final Codec<DialoguePart> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("text").forGetter(DialoguePart::text),
            Face.CODEC.optionalFieldOf("face", Face.NEUTRAL).forGetter(DialoguePart::face),
            Codec.BOOL.optionalFieldOf("requires_interaction", false).forGetter(DialoguePart::requiresInteraction)
    ).apply(instance, DialoguePart::new));

    private static final Codec<DialoguePart> SIMPLE_CODEC = Codec.STRING.xmap(
            DialoguePart::of, DialoguePart::text
    );

    /**
     * Accepts either a bare string or the full object form; plain parts encode back to a bare string.
     */
    public static final Codec<DialoguePart> CODEC = CodecUtils.bestAlternative(FULL_CODEC, SIMPLE_CODEC,
            (d1, d2) -> d1.face != Face.NEUTRAL || d1.requiresInteraction);

    public static DialoguePart of(String text) {
        return new DialoguePart(text, Face.NEUTRAL, false);
    }
}
