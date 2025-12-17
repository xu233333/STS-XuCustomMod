package XuCustomMod.Utils;

import XuCustomMod.XuCustomMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.*;

import java.lang.reflect.Constructor;
import java.util.function.Function;

public class ActionRegistries {
    public static Function<Integer, AbstractGameAction> GainEnergyAction = GainEnergyAction::new;
    public static Function<Integer, AbstractGameAction> IncreaseMaxOrbAction = IncreaseMaxOrbAction::new;
    public static Function<Integer, AbstractGameAction> SetMaxOrbAction = (level) -> {
        int playerNowOrbs = AbstractDungeon.player.maxOrbs;
        return new IncreaseMaxOrbAction(level - playerNowOrbs);
    };
    public static Function<Integer, AbstractGameAction> IncreaseMaxOrbActionWhenPlayerHasOrb = (level) -> {
        if (AbstractDungeon.player.maxOrbs > 0) {
            int playerNowOrbs = AbstractDungeon.player.maxOrbs;
            return new IncreaseMaxOrbAction(level - playerNowOrbs);
        }
        return null;
    };
    public static Function<Integer, AbstractGameAction> SetMaxOrbActionWhenPlayerHasOrb = (level) -> {
        if (AbstractDungeon.player.maxOrbs > 0) {
            int playerNowOrbs = AbstractDungeon.player.maxOrbs;
            return new IncreaseMaxOrbAction(level - playerNowOrbs);
        }
        return null;
    };

    public static final class UTILS_PowerPlayerPlaceHolder {};
    public static final UTILS_PowerPlayerPlaceHolder UTILS_PowerPlayerPlaceHolder = new UTILS_PowerPlayerPlaceHolder();
    public static final class UTILS_PowerLevelPlaceHolder {};
    public static final UTILS_PowerLevelPlaceHolder UTILS_PowerLevelPlaceHolder = new UTILS_PowerLevelPlaceHolder();

    public static <T extends AbstractPower> Function<Integer, AbstractGameAction> createApplyPowerAction(Class<T> powerClass, Object... args) {
        Constructor<?> constructor = powerClass.getDeclaredConstructors()[0];
        return (level) -> {
            Object[] newArgs = new Object[args.length];
            for (int argIndex = 0; argIndex < args.length; argIndex++)  {
                if (args[argIndex] instanceof UTILS_PowerPlayerPlaceHolder) {
                    newArgs[argIndex] = AbstractDungeon.player;
                } else if (args[argIndex] instanceof UTILS_PowerLevelPlaceHolder) {
                    newArgs[argIndex] = level;
                } else {
                    newArgs[argIndex] = args[argIndex];
                }
            }
            try {
                AbstractPower power = (AbstractPower) constructor.newInstance(newArgs);
                return new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, power, level);
            } catch (Exception e) {
                XuCustomMod.LOGGER.error("Failed to create power: {}", powerClass.getName());
                return null;
            }
        };
    }

    public static Function<Integer, AbstractGameAction> P_ArtifactPower = createApplyPowerAction(ArtifactPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 人工制品
    public static Function<Integer, AbstractGameAction> P_BarricadePower = createApplyPowerAction(BarricadePower.class, UTILS_PowerPlayerPlaceHolder);  // 壁垒
    public static Function<Integer, AbstractGameAction> P_RegenPower = createApplyPowerAction(RegenPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 再生
    public static Function<Integer, AbstractGameAction> P_BufferPower = createApplyPowerAction(BufferPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 缓冲
    public static Function<Integer, AbstractGameAction> P_RitualPower = createApplyPowerAction(RitualPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder, true);  // 仪式
    public static Function<Integer, AbstractGameAction> P_StrengthPower = createApplyPowerAction(StrengthPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 力量
    public static Function<Integer, AbstractGameAction> P_DexterityPower = createApplyPowerAction(DexterityPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 敏捷
    public static Function<Integer, AbstractGameAction> P_FocusPower = createApplyPowerAction(FocusPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 集中
    public static Function<Integer, AbstractGameAction> P_ThornsPower = createApplyPowerAction(ThornsPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 荆棘
    public static Function<Integer, AbstractGameAction> P_PlatedArmorPower = createApplyPowerAction(PlatedArmorPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 多层护甲
    public static Function<Integer, AbstractGameAction> P_MetallicizePower = createApplyPowerAction(MetallicizePower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 金属化
    public static Function<Integer, AbstractGameAction> P_EnvenomPower = createApplyPowerAction(EnvenomPower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 涂毒
    public static Function<Integer, AbstractGameAction> P_RupturePower = createApplyPowerAction(RupturePower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 中毒
    public static Function<Integer, AbstractGameAction> P_AfterImagePower = createApplyPowerAction(AfterImagePower.class, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 余像
}
