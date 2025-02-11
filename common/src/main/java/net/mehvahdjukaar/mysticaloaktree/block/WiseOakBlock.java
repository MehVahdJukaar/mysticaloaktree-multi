package net.mehvahdjukaar.mysticaloaktree.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentTarget;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WiseOakBlock extends HorizontalDirectionalBlock implements EntityBlock {

    public static final MapCodec<WiseOakBlock> CODEC = simpleCodec(WiseOakBlock::new);

    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

    public WiseOakBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(STATE, State.NONE));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE, FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WiseOakTile(pos, state);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        State s = state.getValue(STATE);
        if (s.isBlinking()) {
            level.setBlock(pos, state.setValue(STATE, State.getNonBlinking(s)), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof WiseOakTile tile) {
            return tile.onInteract(state, level, pos, player, hand);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof WiseOakTile tile) {
            tile.onAttack(state, level, pos, player);
        }
        super.attack(state, level, pos, player);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return getTicker(pBlockEntityType, MysticalOakTree.TILE.get(), WiseOakTile::tick);
    }

    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity) {
        return EntityBlock.super.getListener(level, blockEntity);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        //anger particles
        if (id == 1) {
            if (level.isClientSide) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    ParticleUtils.spawnParticlesOnBlockFace(level, pos, ParticleTypes.ANGRY_VILLAGER, UniformInt.of(1, 1), d, () -> Vec3.ZERO, 0.55);
                }
            }
            return true;
        }
        return super.triggerEvent(state, level, pos, id, param);
    }

    //TODO: add cool enchant particle stuff


    private static final List<BlockPos> KNOWLEDGE_PARTICLE_POS = BlockPos.betweenClosedStream(-2, -2, -2, 2, 1, 2)
            .filter(blockPos -> Math.abs(blockPos.getX()) == 2 || Math.abs(blockPos.getZ()) == 2)
            .map(BlockPos::immutable)
            .toList();


    private static final List<BlockPos> DESTROY_PARTICLE_POS = BlockPos.betweenClosedStream(-3, -3, -2, 3, 2, 3)
            .filter(blockPos -> {
                var l = Vec3.atCenterOf(blockPos).length();
                return l > 2.5f && l < 3.5f;
            })
            .map(BlockPos::immutable)
            .toList();

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(STATE) == State.SLEEPING && random.nextInt(14) == 0) {
            BlockPos targetPos = KNOWLEDGE_PARTICLE_POS.get(level.random.nextInt(KNOWLEDGE_PARTICLE_POS.size()));
            spawnEnchantParticle(level, pos.above(), random, targetPos);
        }
    }

    private static void spawnEnchantParticle(Level level, BlockPos pos, RandomSource random, BlockPos targetPos) {
        level.addParticle(
                ParticleTypes.ENCHANT,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                targetPos.getX() + random.nextFloat() - 0.5,
                targetPos.getY() + random.nextFloat() - 0.5,
                targetPos.getZ() + random.nextFloat() - 0.5
        );
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        super.spawnDestroyParticles(level, player, pos, state);
        //brittle
        ItemStack heldItem = player.getItemInHand(player.getUsedItemHand());
        //mostly silk touch... not perfect
        if (!EnchantmentHelper.hasTag(heldItem, EnchantmentTags.PREVENTS_INFESTED_SPAWNS)) {
            for (int i = 0; i < 30; i++) {
                BlockPos targetPos = DESTROY_PARTICLE_POS.get(level.random.nextInt(DESTROY_PARTICLE_POS.size()));

                spawnEnchantParticle(level, pos.offset(targetPos), level.random, targetPos.multiply(-1));
            }
        }
    }

    @PlatformOnly({PlatformOnly.FORGE})
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return 15;
    }

    public enum State implements StringRepresentable {
        NONE("none"),
        BLINKING("blinking"),
        SLEEPING("sleeping"),
        ANGRY("angry"),
        ANGRY_BLINKING("angry_blinking"),
        BLOWING("blowing"),
        THICC("thicc");

        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public boolean canBlink() {
            return this == NONE || this == ANGRY;
        }

        public boolean isBlinking() {
            return this == BLINKING || this == ANGRY_BLINKING;
        }

        public static State getBlinking(State s) {
            if (s == ANGRY || s == BLOWING || s == THICC) return ANGRY_BLINKING;
            else return BLINKING;
        }

        public static State getNonBlinking(State s) {
            if (s == ANGRY_BLINKING) return ANGRY;
            else return NONE;
        }

        public boolean canSleep() {
            return this == NONE || this == BLINKING;
        }

        public boolean isAngry() {
            return this == ANGRY || this == ANGRY_BLINKING;
        }

        public boolean isBlowing() {
            return this == BLOWING || this == THICC;
        }
    }
}
