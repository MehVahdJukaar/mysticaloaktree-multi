package net.mehvahdjukaar.mysticaloaktree.block;

import net.minecraft.world.level.Level;

public class Relationship {
    private static final int TIME_BETWEEN_CONVERSATIONS = 3 * 18;
    private static final int HURT_DECREMENT = 20;
    private static final int CONVERSATION_INCREMENT = 5;
    private int trust;
    private long lastInteractedTimeStamp;

    public Relationship(Integer trust) {
        this.trust = trust;
        this.lastInteractedTimeStamp = 0;
    }

    public boolean checkTalkCooldown(Level level) {
        long time = level.getGameTime();
        if (Math.abs(time - lastInteractedTimeStamp) > TIME_BETWEEN_CONVERSATIONS) {
            lastInteractedTimeStamp = time;
            if (trust < 100) trust += CONVERSATION_INCREMENT;
            return true;
        }
        return false;
    }

    public void decrease() {
        this.trust = Math.max(0, trust- HURT_DECREMENT);
    }

    //blows away
    public boolean isAngry() {
        return trust < 0;
    }

    //rotates at start of conversation
    public boolean isFriendlyAt() {
        return trust > 40;
    }

    //keeps track during conversatin
    public boolean isInConfidence() {
        return trust > 70;
    }

    public int getTrust() {
        return trust;
    }

}