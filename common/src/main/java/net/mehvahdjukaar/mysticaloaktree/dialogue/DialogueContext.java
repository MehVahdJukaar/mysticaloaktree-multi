package net.mehvahdjukaar.mysticaloaktree.dialogue;

import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.Stat;
import net.mehvahdjukaar.mysticaloaktree.dialogue.stat.StatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * Everything a {@link net.mehvahdjukaar.mysticaloaktree.dialogue.condition.Requirement} may need
 * to decide whether a line is eligible. Assembled (client side) at the moment a trigger fires from
 * the synced tile stats plus live world state. This is the single object both the predicate
 * evaluator and — later — the LLM prompt builder will consume, so new game-event context is added
 * here once and every consumer benefits.
 */
public record DialogueContext(
        Trigger trigger,
        Level level,
        BlockPos pos,
        @Nullable Player player,
        StatMap playerStats,
        StatMap globalStats
) {

    public int stat(Stat stat) {
        return playerStats.get(stat);
    }

    public boolean isNight() {
        return level.isNight();
    }

    public boolean isRaining() {
        return level.isRainingAt(pos.above());
    }

    public boolean isThundering() {
        return level.isThundering();
    }
}
