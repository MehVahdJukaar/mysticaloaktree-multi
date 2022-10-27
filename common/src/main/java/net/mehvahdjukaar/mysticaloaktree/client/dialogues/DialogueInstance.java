package net.mehvahdjukaar.mysticaloaktree.client.dialogues;

import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.client.dialogues.ITreeDialogue;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

//client side class
public class DialogueInstance {

    private static final int TIME_PER_LINE = 3 * 17;
    private static final int WAIT_TIME =2 * 20;
    private static final int MAX_DISTANCE_SQ = 6 * 6;

    private final ITreeDialogue dialogue;
    private int timeUntilNextLine = 0;
    private int lineIndex = 0;
    private boolean waiting = false;

    DialogueInstance(ITreeDialogue dialogue) {
        this.dialogue = dialogue;
    }

    /**
     * @return true if dialogue has not finished yet
     */
    public boolean tick(BlockPos pos) {
        if (timeUntilNextLine == 0) {
            if (waiting) return true; // discard if wait time elapsed
            var status = dialogue.getLine(lineIndex, false);
            if (status == ITreeDialogue.Status.WAITING) {
                timeUntilNextLine = WAIT_TIME;
                waiting = true;
            } else if (status == ITreeDialogue.Status.DONE) {
                return false;
            } else {
                talk(status.text(), pos);
                return true;
            }
        } else {
            timeUntilNextLine--;
        }
        return true;
    }

    public boolean interact(BlockPos pos) {
        if (waiting) {
            var status = dialogue.getLine(lineIndex, true);
            if (status != ITreeDialogue.Status.WAITING && status != ITreeDialogue.Status.DONE) {
                talk(status.text(), pos);
                return true;
            }
        }
        return false;
    }

    private void talk(String text, BlockPos pos) {
        Player player = Minecraft.getInstance().player;
        if (player != null && pos.distToCenterSqr(player.position()) < MAX_DISTANCE_SQ) {
            player.displayClientMessage(TreeLoreManager.formatText(text, player), true);
        }
        timeUntilNextLine = TIME_PER_LINE;
        lineIndex++;
        waiting = false;
    }


}
