package net.mehvahdjukaar.mysticaloaktree.block;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.Nullable;

//TODO: add horizontal rotation
public class WiseOakBlock extends HorizontalDirectionalBlock implements EntityBlock {


    public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);


    public WiseOakBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH).setValue(STATE, State.NONE));
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof WiseOakTile tile) {
            return tile.onInteract(state, level, pos, player, hand);
        }
        return InteractionResult.PASS;
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

    @javax.annotation.Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> getTicker(BlockEntityType<A> type, BlockEntityType<E> targetType, BlockEntityTicker<? super E> ticker) {
        return targetType == type ? (BlockEntityTicker<A>) ticker : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> GameEventListener getListener(ServerLevel level, T blockEntity) {
        return EntityBlock.super.getListener(level, blockEntity);
    }


    public enum State implements StringRepresentable {
        NONE("none"),
        BLINKING("blinking"),
        SLEEPING("sleeping"),
        ANGRY("angry"),
        ANGRY_BLINKING("angry_blinking"),
        BLOWING("blowing");

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
            if (s == ANGRY || s == BLOWING) return ANGRY_BLINKING;
            else return BLINKING;
        }

        public static State getNonBlinking(State s) {
            if (s == ANGRY_BLINKING) return ANGRY;
            else return NONE;
        }

        public boolean canSleep() {

            return this == NONE || this == BLINKING;
        }
    }
}
