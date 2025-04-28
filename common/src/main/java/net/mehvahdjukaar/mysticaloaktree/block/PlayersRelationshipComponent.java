package net.mehvahdjukaar.mysticaloaktree.block;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;

import java.util.Map;
import java.util.UUID;

public record PlayersRelationshipComponent(Map<UUID, Relationship> map) {

    public static final Codec<PlayersRelationshipComponent> CODEC = Codec.unboundedMap(
            UUIDUtil.STRING_CODEC,
            Codec.INT.xmap(Relationship::new, Relationship::getTrust)
    ).xmap(PlayersRelationshipComponent::new, PlayersRelationshipComponent::map);

}
