package net.mehvahdjukaar.mysticaloaktree.dialogue.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Registry + dispatch codec for {@link Effect} types. */
public final class Effects {

    private static final Map<String, MapCodec<? extends Effect>> BY_NAME = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends Effect>, String> BY_CODEC = new IdentityHashMap<>();

    public static final Codec<MapCodec<? extends Effect>> TYPE_CODEC = Codec.STRING.flatXmap(
            name -> {
                MapCodec<? extends Effect> codec = BY_NAME.get(name);
                return codec != null ? DataResult.success(codec)
                        : DataResult.error(() -> "Unknown effect type: " + name);
            },
            codec -> {
                String name = BY_CODEC.get(codec);
                return name != null ? DataResult.success(name)
                        : DataResult.error(() -> "Unregistered effect codec");
            });

    public static final Codec<Effect> CODEC = TYPE_CODEC.dispatch(
            "type", Effect::codec, Function.identity());

    public static final MapCodec<StatEffect> STAT = register("stat", StatEffect.CODEC);

    private Effects() {
    }

    public static <T extends Effect> MapCodec<T> register(String name, MapCodec<T> codec) {
        BY_NAME.put(name, codec);
        BY_CODEC.put(codec, name);
        return codec;
    }

    public static void init() {
    }
}
