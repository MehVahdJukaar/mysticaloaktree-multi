package net.mehvahdjukaar.mysticaloaktree.client.dialogues;

import com.mojang.serialization.Codec;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TreeDialogueTypes {

    private static final Map<String, ITreeDialogue.Type<?>> TYPES = new HashMap<>();


    public static final ITreeDialogue.Type<WokenUp> WOKEN_UP = register(new ITreeDialogue.Type<>(
            WokenUp.CODEC, "woken_up", 45));

    public static final ITreeDialogue.Type<TalkedTo> TALKED_TO = register(new ITreeDialogue.Type<>(
            TalkedTo.CODEC, "talked_to", 31));

    public static final ITreeDialogue.Type<Hurt> HURT = register(new ITreeDialogue.Type<>(
            Hurt.CODEC, "hurt", 61));

    public static final ITreeDialogue.Type<HeardSound> HEARD_SOUND = register(new ITreeDialogue.Type<>(
            HeardSound.CODEC, "heard_sound", 31));

    public static final ITreeDialogue.Type<ProjectileHit> PROJECTILE_HIT = register(new ITreeDialogue.Type<>(
            ProjectileHit.CODEC, "projectile_hit", 31));


    public static final ITreeDialogue.Type<EntityAround> ENTITY_AROUND = register(new ITreeDialogue.Type<>(
            EntityAround.CODEC, "entity_around", 31));

    public static final ITreeDialogue.Type<OnBroken> ON_BROKEN = register(new ITreeDialogue.Type<>(
            OnBroken.CODEC, "ok_broken", 31));


    public static final ITreeDialogue.Type<ITreeDialogue> NO_OP = register(new ITreeDialogue.Type<>(
            Codec.unit(ITreeDialogue.NO_OP), "no_op", 100));


    public static void init() {

    }

    public static Optional<? extends ITreeDialogue.Type<? extends ITreeDialogue>> get(String name) {
        return Optional.ofNullable(TYPES.get(name));
    }

    public static <B extends ITreeDialogue.Type<?>> B register(B newType) {
        TYPES.put(newType.name(), newType);
        return newType;
    }


}
