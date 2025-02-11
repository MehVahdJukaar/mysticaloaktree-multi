package net.mehvahdjukaar.mysticaloaktree.block;

import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.DialogueInstance;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.TreeDialogueTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WiseOakTile extends BlockEntity {

    public static final int BLOW_DURATION = 17 * 2;
    public static final int FOLLOW_TIME = 20 * 3;
    public static final int DIALOGUES_TO_SLEEP = 5;
    public static final int BLINK_TIME = 5;
    private static final double BLOW_DIST = 11;
    private static final double THICC_CHANCE = 0.03;


    private final Map<UUID, Relationship> playerRelationship = new HashMap<>();


    //common stuff
    private Player playerTarget;
    private int blowCounter;
    private int followCounter;

    private int dialoguesUntilSlept = 0;

    //client stuff
    @Nullable
    private DialogueInstance currentDialogue = null;

    public WiseOakTile(BlockPos blockPos, BlockState blockState) {
        super(MysticalOakTree.TILE.get(), blockPos, blockState);
    }

    @NotNull
    private Relationship getRelationship(Player player) {
        return playerRelationship.computeIfAbsent(player.getUUID(), u -> new Relationship(0));
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WiseOakTile tile) {

        if (tile.blowCounter > 0) {
            tile.blowCounter--;
        }
        if (state.getValue(WiseOakBlock.STATE).isBlowing()) {

            if (tile.blowCounter <= 0) {
                tile.stopBlowing(level, pos, state, tile);
            }

            if (tile.playerTarget != null) {
                if (isInLineOfSight(state.getValue(WiseOakBlock.FACING), pos, level, tile.playerTarget)) {
                    if (level.isClientSide) {
                        tile.blowParticles(state, level, pos);
                    }
                    tile.blowPlayer(state, level, pos);
                }
            }
        }

        if (level.isClientSide) {
            if (tile.currentDialogue != null) {
                if (!tile.currentDialogue.tick(pos)) {
                    tile.currentDialogue = null;
                }
            }


        } else {

            if (tile.followCounter > 0) {
                if (tile.playerTarget != null) {
                    rotateTowardPlayer(state, level, pos, tile.playerTarget);
                }
                tile.followCounter--;
                if (tile.followCounter == 0) tile.playerTarget = null;
            }

            //extra random tick
            if (tile.blowCounter == 0 && level.getGameTime() % 23 == 0 && level.random.nextInt(21) == 0) {
                //add random tick here and ditch block one
                //tries to force sleep
                tile.randomTick(state, (ServerLevel) level, pos, level.random);

            }
        }
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        WiseOakBlock.State s = state.getValue(WiseOakBlock.STATE);
        if (s == WiseOakBlock.State.ANGRY && random.nextInt(3) == 0) {
            level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.NONE));
            return;
        }
        boolean isDay = !level.isNight();
        if (s.canSleep() && (isDay || isTiredOfYou(state, level))) {
            this.goToSleep(level, pos, state);
            return;
        } else if (s == WiseOakBlock.State.SLEEPING && !isDay) {
            this.wakeUp(level, pos, state);
            return;
        }
        if (s.canBlink() && random.nextFloat() < 1) {
            //replace with tile tick stuff
            level.scheduleTick(pos, state.getBlock(), BLINK_TIME);
            level.setBlock(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.getBlinking(s)), 3);
        }
    }

    private boolean isTiredOfYou(BlockState state, ServerLevel level) {
        int dial = this.dialoguesUntilSlept - DIALOGUES_TO_SLEEP;
        int perc = Mth.clamp(8 - dial, 1, 8);
        return state.getValue(WiseOakBlock.STATE) == WiseOakBlock.State.NONE &&
                level.random.nextInt(perc) == 0;
    }

    private void goToSleep(Level level, BlockPos pos, BlockState state) {
        this.playerTarget = null; //use level event to clear dialogue
        this.followCounter = 0;
        this.blowCounter = 0;
        level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.SLEEPING));
    }

    private void wakeUp(Level level, BlockPos pos, BlockState state) {
        this.dialoguesUntilSlept = 0;
        level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.NONE));
    }

    public ItemInteractionResult onInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        var treeState = state.getValue(WiseOakBlock.STATE);
        if (treeState.isAngry()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        Relationship r = getRelationship(player);
        boolean wokenUp = treeState == WiseOakBlock.State.SLEEPING;
        if (wokenUp || r.checkTalkCooldown(level)) {
            this.dialoguesUntilSlept++;
            if (level.isClientSide) {
                DialogueInstance dialogue = getOrCreateDialogue(
                        wokenUp ? TreeDialogueTypes.WOKEN_UP : TreeDialogueTypes.TALKED_TO,
                        level.random, r);
                if (dialogue != null) {
                    dialogue.interact(pos);
                }
            } else {

                if (wokenUp || r.isInConfidence()) {
                    this.setTrackedTarget(player);
                }
                if (wokenUp || r.isFriendlyAt()) {
                    rotateTowardPlayer(state, level, pos, player);
                }
            }
            if (wokenUp) {
                wakeUp(level, pos, state);
                r.decrease();
                spawnAngryParticles(level, pos, state);
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    private DialogueInstance getOrCreateDialogue(ITreeDialogue.Type<?> source, RandomSource randomSource, Relationship r) {
        if (this.currentDialogue == null) {
            createRandomDialogue(source, randomSource, r);
        }
        return this.currentDialogue;
    }

    private DialogueInstance createRandomDialogue(ITreeDialogue.Type<?> source, RandomSource randomSource, Relationship r) {
        ITreeDialogue dialogue = TreeLoreManager.getRandomDialogue(source, randomSource, r.getTrust());
        if (dialogue != null) {
            this.currentDialogue = dialogue.createInstance();
            return this.currentDialogue;
        }
        return null;
    }

    public void onAttack(BlockState state, Level level, BlockPos pos, Player player) {
        //particles
        Relationship r = getRelationship(player);
        spawnAngryParticles(level, pos, state);
        // if(this.blowCounter==0)
        this.startBlowingAt(player, state, pos, level);

        if (level.isClientSide) {
            DialogueInstance dialogue = createRandomDialogue(TreeDialogueTypes.HURT, level.random, r);
            if (dialogue != null) {
                dialogue.tick(pos);
            }
        }
        r.decrease();
    }

    private void spawnAngryParticles(Level level, BlockPos pos, BlockState state) {
        level.blockEvent(pos, state.getBlock(), 1, 0);
    }

    private void blowParticles(BlockState state, Level level, BlockPos pos) {
        if (playerTarget != null) {
            Direction dir = state.getValue(WiseOakBlock.FACING);
            Vec3 p = Vec3.atCenterOf(pos);

            p = p.add(MthUtils.V3itoV3(dir.getNormal()).scale(0.6));
            Vec3 speed = p.subtract(playerTarget.position().add(0, playerTarget.getEyeHeight() * 2 / 3f, 0));
            speed = speed.normalize();

            speed = speed.scale(-0.4f);
            for (int j = 0; j < 2; ++j) {
                level.addParticle(MysticalOakTree.WIND.get(),
                        p.x + (level.random.nextFloat() - level.random.nextFloat()) * 0.05f,
                        p.y - 0.33 + (level.random.nextFloat() - level.random.nextFloat()) * 0.05f,
                        p.z,
                        speed.x, speed.y, speed.z);
            }
        }
    }


    private void blowPlayer(BlockState state, Level level, BlockPos pos) {
        if (playerTarget != null) {
            double dist = pos.distToCenterSqr(playerTarget.position());
            double max = BLOW_DIST * BLOW_DIST;
            if (dist < max) {
                double strength = 1 - dist / max;
                Vec3 direction = getViewVector(pos, playerTarget);

                strength *= 1.0 - 0.25 * playerTarget.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                direction = direction.scale(strength * 0.25);

                var vec3 = playerTarget.getDeltaMovement();
                playerTarget.setDeltaMovement(vec3.x + direction.x,
                        vec3.y + (playerTarget.onGround() ? 0f : 0),
                        vec3.z + direction.z);
            }
        }
    }

    private static Vec3 getViewVector(BlockPos pos, Entity entity) {
        Vec3 p = Vec3.atCenterOf(pos);
        Vec3 speed = entity.position().subtract(p);//.add(0, 0.2, 0));
        speed = speed.normalize();
        return speed;
    }

    private void stopBlowing(Level level, BlockPos pos, BlockState state, WiseOakTile tile) {
        tile.playerTarget = null;
        tile.followCounter = 0;
        level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.ANGRY));
        //wake up
    }

    private void startBlowingAt(Player player, BlockState state, BlockPos pos, Level level) {
        //rotate toward
        rotateTowardPlayer(state, level, pos, player);
        this.blowCounter = BLOW_DURATION;
        //blow immediately

        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(WiseOakBlock.STATE,
                level.random.nextFloat() < THICC_CHANCE ? WiseOakBlock.State.THICC : WiseOakBlock.State.BLOWING));

        this.setTrackedTarget(player);
    }

    private void setTrackedTarget(Player player) {
        this.playerTarget = player;
        this.followCounter = FOLLOW_TIME;
    }

    public static void rotateTowardPlayer(BlockState state, Level level, BlockPos pos, Player player) {
        //rotate toward
        Vec3 v = player.position().subtract(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        var d = Direction.getNearest(v.x, 0, v.z);
        if (d.getAxis() != Direction.Axis.Y && state.getValue(HorizontalDirectionalBlock.FACING) != d) {
            level.setBlockAndUpdate(pos, state.setValue(HorizontalDirectionalBlock.FACING, d));
        }
    }


    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (var v : playerRelationship.entrySet()) {
            CompoundTag comp = new CompoundTag();
            comp.putInt("trust", v.getValue().getTrust());
            comp.putUUID("id", v.getKey());
            list.add(comp);
        }
        if (!list.isEmpty()) {
            tag.put("relationship", list);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.playerRelationship.clear();
        ListTag list = tag.getList("relationship", 10);
        if (list != null) {
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag effectsCompound = list.getCompound(i);
                UUID id = effectsCompound.getUUID("id");
                Integer level = effectsCompound.getInt("trust");
                if (id != null) {
                    this.playerRelationship.put(id, new Relationship(level));
                }
            }
        }
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    private static boolean isInLineOfSight(Direction dir, BlockPos pos, Level level, Entity target) {
        Vec3 startPos = Vec3.atCenterOf(pos).relative(dir, 0.6);
        if (target.distanceToSqr(startPos) > BLOW_DIST * BLOW_DIST) return false;

        return clip(level, startPos, target.getEyePosition()).getType() == HitResult.Type.MISS;
    }

    public static BlockHitResult clip(Level level, Vec3 startPos, Vec3 endPos) {

        var blockGetter = ClipContext.Block.COLLIDER;
        var fluidGetter = ClipContext.Fluid.NONE;
        var collision = CollisionContext.empty();

        return BlockGetter.traverseBlocks(startPos, endPos, null, (Null, pos) -> {
            BlockState blockstate = level.getBlockState(pos);
            FluidState fluidstate = level.getFluidState(pos);

            VoxelShape voxelShape = blockGetter.get(blockstate, level, pos, collision);

            BlockHitResult blockHitResult = level.clipWithInteractionOverride(startPos, endPos, pos, voxelShape, blockstate);
            VoxelShape fluidShape = fluidGetter.canPick(fluidstate) ? fluidstate.getShape(level, pos) : Shapes.empty();
            BlockHitResult fluidHirResult = fluidShape.clip(startPos, endPos, pos);
            double d0 = blockHitResult == null ? Double.MAX_VALUE : startPos.distanceToSqr(blockHitResult.getLocation());
            double d1 = fluidHirResult == null ? Double.MAX_VALUE : startPos.distanceToSqr(fluidHirResult.getLocation());
            return d0 <= d1 ? blockHitResult : fluidHirResult;
        }, arg -> {
            Vec3 vec3 = startPos.subtract(endPos);
            return BlockHitResult.miss(endPos, Direction.getNearest(vec3.x, vec3.y, vec3.z), BlockPos.containing(endPos));
        });
    }


}
