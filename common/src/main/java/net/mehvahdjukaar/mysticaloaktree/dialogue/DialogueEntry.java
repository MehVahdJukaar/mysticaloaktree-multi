package net.mehvahdjukaar.mysticaloaktree.dialogue;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.dialogue.condition.Requirement;
import net.mehvahdjukaar.mysticaloaktree.dialogue.condition.StatRequirement;
import net.mehvahdjukaar.mysticaloaktree.dialogue.effect.Effect;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stats;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A single data-driven dialogue: which trigger it answers, the requirements gating it, the effects
 * it would apply, and the lines it streams. {@link #id} is the file location (not serialized),
 * assigned on load and used to derive lang-override keys.
 *
 * <p>{@link #CODEC} reads both the new schema and the legacy one (a {@code "type"} + {@code "text"} +
 * {@code "trust_required"} file), so the existing ~286 dialogue jsons keep working untouched.
 */
public record DialogueEntry(
        @Nullable ResourceLocation id,
        String trigger,
        List<Requirement> requirements,
        List<Effect> effects,
        List<DialoguePart> parts,
        double weight
) {

    // ---- new schema ----
    private static final Codec<DialogueEntry> NEW_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("trigger").forGetter(DialogueEntry::trigger),
            Requirement.CODEC.listOf().optionalFieldOf("require", List.of()).forGetter(DialogueEntry::requirements),
            Effect.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(DialogueEntry::effects),
            DialoguePart.CODEC.listOf().fieldOf("lines").forGetter(DialogueEntry::parts),
            Codec.DOUBLE.optionalFieldOf("weight", 1.0).forGetter(DialogueEntry::weight)
    ).apply(instance, (trigger, req, eff, parts, weight) ->
            new DialogueEntry(null, trigger, req, eff, parts, weight)));

    // ---- legacy schema (type / trust_required / text [+ required_interactions]) ----
    private static final Codec<List<String>> LEGACY_TEXT_CODEC = Codec.either(Codec.STRING, Codec.STRING.listOf())
            .xmap(e -> e.map(List::of, Function.identity()), List::getFirst);

    private static final Codec<DialogueEntry> LEGACY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("type").forGetter(DialogueEntry::trigger),
            Codec.INT.optionalFieldOf("trust_required", 0).forGetter(e -> 0),
            LEGACY_TEXT_CODEC.fieldOf("text").forGetter(e -> e.parts.stream().map(DialoguePart::text).toList()),
            Codec.BOOL.listOf().optionalFieldOf("required_interactions", List.of()).forGetter(e -> List.of())
    ).apply(instance, DialogueEntry::fromLegacy));

    public static final Codec<DialogueEntry> CODEC = Codec.either(NEW_CODEC, LEGACY_CODEC)
            .xmap(either -> either.map(Function.identity(), Function.identity()), Either::left);

    private static DialogueEntry fromLegacy(String type, int trustRequired, List<String> text, List<Boolean> interactions) {
        List<DialoguePart> parts = new ArrayList<>();
        for (int i = 0; i < text.size(); i++) {
            // old semantics: required_interactions[i-1] gates part i (i >= 1)
            boolean wait = i >= 1 && (i - 1) < interactions.size() && interactions.get(i - 1);
            parts.add(new DialoguePart(text.get(i), Face.NEUTRAL, wait));
        }
        List<Requirement> req = List.of(StatRequirement.atLeast(Stats.TRUST, trustRequired));
        return new DialogueEntry(null, type, req, List.of(), parts, 1.0);
    }

    /** Programmatic entry (e.g. online-fetched flavor lines) with no requirements. */
    public static DialogueEntry simple(Trigger trigger, List<String> lines) {
        return new DialogueEntry(null, trigger.id(), List.of(), List.of(),
                lines.stream().map(DialoguePart::of).toList(), 1.0);
    }

    public DialogueEntry withId(ResourceLocation id) {
        return new DialogueEntry(id, trigger, requirements, effects, parts, weight);
    }

    public boolean matches(DialogueContext context) {
        for (Requirement r : requirements) {
            if (!r.test(context)) return false;
        }
        return true;
    }

    /** Lang key a translation pack may define to override {@code parts().get(index).text()}. */
    public String langKey(int index) {
        String path = id == null ? "anonymous" : id.getPath().replace('/', '.');
        return "tree.line." + path + "." + index;
    }
}
