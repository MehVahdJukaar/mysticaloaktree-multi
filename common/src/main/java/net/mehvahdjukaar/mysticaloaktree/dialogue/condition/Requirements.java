package net.mehvahdjukaar.mysticaloaktree.dialogue.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/** Registry + dispatch codec for {@link Requirement} types. */
public final class Requirements {

    private static final Map<String, MapCodec<? extends Requirement>> BY_NAME = new LinkedHashMap<>();
    private static final Map<MapCodec<? extends Requirement>, String> BY_CODEC = new IdentityHashMap<>();

    public static final Codec<MapCodec<? extends Requirement>> TYPE_CODEC = Codec.STRING.flatXmap(
            name -> {
                MapCodec<? extends Requirement> codec = BY_NAME.get(name);
                return codec != null ? DataResult.success(codec)
                        : DataResult.error(() -> "Unknown requirement type: " + name);
            },
            codec -> {
                String name = BY_CODEC.get(codec);
                return name != null ? DataResult.success(name)
                        : DataResult.error(() -> "Unregistered requirement codec");
            });

    public static final Codec<Requirement> CODEC = TYPE_CODEC.dispatch(
            "type", Requirement::codec, Function.identity());

    // built-ins (registered after CODEC is assigned so combinators can reference it)
    public static final MapCodec<StatRequirement> STAT = register("stat", StatRequirement.CODEC);
    public static final MapCodec<TimeRequirement> TIME = register("time", TimeRequirement.CODEC);
    public static final MapCodec<WeatherRequirement> WEATHER = register("weather", WeatherRequirement.CODEC);
    public static final MapCodec<ModLoadedRequirement> MOD_LOADED = register("mod_loaded", ModLoadedRequirement.CODEC);
    public static final MapCodec<CompoundRequirement.AllOf> ALL_OF = register("all_of", CompoundRequirement.AllOf.CODEC);
    public static final MapCodec<CompoundRequirement.AnyOf> ANY_OF = register("any_of", CompoundRequirement.AnyOf.CODEC);
    public static final MapCodec<CompoundRequirement.Not> NOT = register("not", CompoundRequirement.Not.CODEC);

    private Requirements() {
    }

    public static <T extends Requirement> MapCodec<T> register(String name, MapCodec<T> codec) {
        BY_NAME.put(name, codec);
        BY_CODEC.put(codec, name);
        return codec;
    }

    public static void init() {
    }
}
