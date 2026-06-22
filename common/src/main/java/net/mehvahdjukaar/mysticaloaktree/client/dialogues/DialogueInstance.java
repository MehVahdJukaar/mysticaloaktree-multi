package net.mehvahdjukaar.mysticaloaktree.client.dialogues;

import net.mehvahdjukaar.mysticaloaktree.client.TreeLoreManager;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialogueEntry;
import net.mehvahdjukaar.mysticaloaktree.dialogue.DialoguePart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/** Client-side playback of a single {@link DialogueEntry}: streams its parts one at a time. */
public class DialogueInstance {

    private static final int TIME_PER_LINE = 3 * 17;
    private static final int WAIT_TIME = 2 * 20;
    private static final int MAX_DISTANCE_SQ = 6 * 6;

    private final DialogueEntry entry;
    private int timeUntilNextLine = 0;
    private int lineIndex = 0;
    private boolean waiting = false;

    public DialogueInstance(DialogueEntry entry) {
        this.entry = entry;
    }

    /** @return true while the dialogue is still playing. */
    public boolean tick(BlockPos pos) {
        if (timeUntilNextLine > 0) {
            timeUntilNextLine--;
            return true;
        }
        if (waiting) return true; // stalled until the player clicks again
        return showLine(pos, false);
    }

    /** Called when the player clicks the tree again; advances a part that was waiting for input. */
    public boolean interact(BlockPos pos) {
        if (waiting) return showLine(pos, true);
        return false;
    }

    private boolean showLine(BlockPos pos, boolean interacted) {
        if (lineIndex >= entry.parts().size()) return false; // done
        DialoguePart part = entry.parts().get(lineIndex);
        if (part.requiresInteraction() && !interacted) {
            timeUntilNextLine = WAIT_TIME;
            waiting = true;
            return true;
        }
        Player player = Minecraft.getInstance().player;
        if (player != null && pos.distToCenterSqr(player.position()) < MAX_DISTANCE_SQ) {
            player.displayClientMessage(resolveText(part, lineIndex, player), true);
        }
        timeUntilNextLine = TIME_PER_LINE;
        lineIndex++;
        waiting = false;
        return true;
    }

    /** Inline default text, overridable by a lang file under {@link DialogueEntry#langKey}. */
    private Component resolveText(DialoguePart part, int index, Player player) {
        String key = entry.langKey(index);
        String text = I18n.exists(key) ? I18n.get(key) : part.text();
        return TreeLoreManager.formatText(text, player);
    }
}
