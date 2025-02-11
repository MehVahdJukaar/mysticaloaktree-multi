package net.mehvahdjukaar.mysticaloaktree.client.dialogues.types;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ProjectileHit(int trust, String text, boolean fromPlayer,
                            Optional<EntityType<?>> targetEntity) implements ITreeDialogue {

    public static final MapCodec<ProjectileHit> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    Codec.intRange(0, 100).fieldOf("trust_required").forGetter(t -> t.trust),
                    Codec.STRING.fieldOf("text").forGetter(t -> t.text),
                    Codec.BOOL.optionalFieldOf("from_player", false).forGetter(t->t.fromPlayer),
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("target_entity").forGetter(t -> t.targetEntity)
            ).apply(instance, ProjectileHit::new));

    @Override
    public Type<ProjectileHit> getType() {
        return TreeDialogueTypes.PROJECTILE_HIT;
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

