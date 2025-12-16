package XuCustomMod.Utils;

import com.megacrit.cardcrawl.actions.AbstractGameAction;

import java.util.function.Consumer;
import java.util.function.Function;

public class LeveledAction {
    public Function<Integer, AbstractGameAction> action;

    public final int[] actionLevels;
    public final boolean AllowZeroToApply;

    public LeveledAction(Function<Integer, AbstractGameAction> action, boolean UseAddMode, boolean AllowZeroToApply, int... actionLevels) {
        this.action = action;
        this.AllowZeroToApply = AllowZeroToApply;
        if (!UseAddMode) {
            this.actionLevels = actionLevels;
        } else {
            this.actionLevels = new int[actionLevels.length];
            if (actionLevels.length != 0) {
                this.actionLevels[0] = actionLevels[0];
                for (int i = 1; i < this.actionLevels.length; i++) {
                    this.actionLevels[i] = this.actionLevels[i - 1] + actionLevels[i];
                }
            }
        }
    }

    private int getActionCurrentLevel(int level) {
        if (this.actionLevels.length == 0) {
            return 0;
        }
        level = Math.max(Math.min(level, this.actionLevels.length - 1), 0);
        return this.actionLevels[level];
    }

    public AbstractGameAction apply(int level) {
        if (this.action == null) {
            return null;
        } else {
            int currentLevel = getActionCurrentLevel(level);
            if (this.AllowZeroToApply || currentLevel != 0) {
                return this.action.apply(currentLevel);
            }
        }
        return null;
    }

    public static void applyAll(Consumer<AbstractGameAction> actionConsumer, int level, LeveledAction... leveledActions) {
        for (LeveledAction leveledAction : leveledActions) {
            AbstractGameAction action = leveledAction.apply(level);
            if (action != null) {
                actionConsumer.accept(action);
            }
        }
    }
}
