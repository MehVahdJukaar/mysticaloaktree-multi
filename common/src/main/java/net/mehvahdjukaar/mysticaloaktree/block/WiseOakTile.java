package net.mehvahdjukaar.mysticaloaktree.block;

import net.mehvahdjukaar.mysticaloaktree.MysticalOakTree;
import net.mehvahdjukaar.mysticaloaktree.client.DialogueInstance;
import net.mehvahdjukaar.mysticaloaktree.client.ITreeDialogue;
import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WiseOakTile extends BlockEntity {

    public static final int BLOW_COOLDOWN_DURATION = 20 * 2;
    public static final int BLOW_DURATION = 20 * 3;
    public static final int FOLLOW_TIME = 20 * 3;
    public static final int DIALOGUES_TO_SLEEP = 8;
    public static final int BLINK_TIME = 5;


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
        if (level.isClientSide) {
            if (tile.currentDialogue != null) {
                if (!tile.currentDialogue.tick(pos)) {
                    tile.currentDialogue = null;
                }
            }
        } else {
            if (tile.blowCounter > 0) {
                tile.blowCounter--;
                if (tile.blowCounter > BLOW_COOLDOWN_DURATION) {
                    if (level.isClientSide) {

                    }
                }
            }
            if (tile.followCounter > 0) {
                if (tile.playerTarget != null) {
                    rotateTowardPlayer(state, level, pos, tile.playerTarget);
                }
                tile.followCounter--;
                if (tile.followCounter == 0) tile.playerTarget = null;
            }

            //extra random tick
            if (tile.blowCounter == 0 && level.getGameTime() % 27 == 0 && level.random.nextInt(50) == 0) {
                //add random tick here and ditch block one
                if (tile.dialoguesUntilSlept > DIALOGUES_TO_SLEEP) {
                    //if neutral and had enough of your shit he goes to sleep
                    if (state.getValue(WiseOakBlock.STATE) == WiseOakBlock.State.NONE) {
                        tile.goToSleep(level, pos, state);
                    }
                } else {
                    //tries to force sleep
                    tile.randomTick(state, (ServerLevel) level, pos, level.random);
                }
            }
        }
    }

    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        WiseOakBlock.State s = state.getValue(WiseOakBlock.STATE);
        boolean night = level.isNight();
        if (s.canSleep() && night) {
            this.goToSleep(level, pos, state);
            return;
        } else if (s == WiseOakBlock.State.SLEEPING && !night) {
            this.wakeUp(level, pos, state);
            return;
        }
        if (s.canBlink() && random.nextFloat() < 1) {
            //replace with tile tick stuff
            level.scheduleTick(pos, state.getBlock(), BLINK_TIME);
            level.setBlock(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.getBlinking(s)), 3);
        }
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

    public InteractionResult onInteract(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        Relationship r = getRelationship(player);
        boolean wokenUp = state.getValue(WiseOakBlock.STATE) == WiseOakBlock.State.SLEEPING;
        if (wokenUp || r.checkTalkCooldown(level)) {
            if (!level.isClientSide) {
                if (wokenUp || r.isInConfidence()) {
                    this.setTarget(player);
                }
                if (wokenUp || r.isFriendlyAt()) {
                    rotateTowardPlayer(state, level, pos, player);
                }
                if (wokenUp) level.setBlockAndUpdate(pos, state.setValue(WiseOakBlock.STATE, WiseOakBlock.State.NONE));
            } else {
                DialogueInstance dialogue = getOrCreateDialogue(
                        wokenUp ? ITreeDialogue.Type.WOKEN_UP : ITreeDialogue.Type.TALKED_WITH,
                        level.random, r);
                if (dialogue != null) {
                    dialogue.interact(pos);
                } else {
                    int aaa = 1;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Nullable
    private DialogueInstance getOrCreateDialogue(ITreeDialogue.Type source, RandomSource randomSource, Relationship r) {
        if (this.currentDialogue == null) {
            createRandomDialogue(source, randomSource, r);
        }
        return this.currentDialogue;
    }

    private DialogueInstance createRandomDialogue(ITreeDialogue.Type source, RandomSource randomSource, Relationship r) {
        ITreeDialogue dialogue = TreeLoreManager.getRandomDialogue(source, randomSource, r.getTrust());
        if (dialogue != null) {
            this.currentDialogue = dialogue.createInstance();
            return this.currentDialogue;
        }
        return null;
    }

    public void onAttack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                ParticleUtils.spawnParticlesOnBlockFace(level, pos, ParticleTypes.ANGRY_VILLAGER, UniformInt.of(1, 2), d, () -> Vec3.ZERO, 0.55);
            }
        }

        Relationship r = getRelationship(player);
        r.decrease();
        if (r.isAngry() && !level.isClientSide) {
            //rotate toward
            rotateTowardPlayer(state, level, pos, player);
            this.setAngerTarget(player);
            //blow immediately
            level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(WiseOakBlock.STATE, WiseOakBlock.State.BLOWING));
        }
        //particles
        if (level.random.nextFloat() < 0.5) {
            DialogueInstance dialogue = createRandomDialogue(ITreeDialogue.Type.HURT, level.random, r);
            if (dialogue != null) {
                dialogue.tick(pos);
            }
        }
    }

    private void setAngerTarget(Player player) {
        this.blowCounter = BLOW_DURATION + BLOW_COOLDOWN_DURATION;
        this.setTarget(player);
    }

    private void setTarget(Player player) {
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
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
    public void load(CompoundTag tag) {
        super.load(tag);
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
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
}
