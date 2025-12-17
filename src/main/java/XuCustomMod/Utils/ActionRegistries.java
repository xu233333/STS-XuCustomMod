package XuCustomMod.Utils;

import XuCustomMod.XuCustomMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.powers.watcher.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ActionRegistries {
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> GainEnergyAction = (target, level) -> new GainEnergyAction(level);
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> IncreaseMaxOrbAction = (target, level) -> new IncreaseMaxOrbAction(level);
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> SetMaxOrbAction = (target, level) -> {
        int playerNowOrbs = AbstractDungeon.player.maxOrbs;
        return new IncreaseMaxOrbAction(level - playerNowOrbs);
    };
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> IncreaseMaxOrbActionWhenPlayerHasOrb = (target, level) -> {
        if (AbstractDungeon.player.maxOrbs > 0) {
            int playerNowOrbs = AbstractDungeon.player.maxOrbs;
            return new IncreaseMaxOrbAction(level - playerNowOrbs);
        }
        return null;
    };
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> SetMaxOrbActionWhenPlayerHasOrb = (target, level) -> {
        if (AbstractDungeon.player.maxOrbs > 0) {
            int playerNowOrbs = AbstractDungeon.player.maxOrbs;
            return new IncreaseMaxOrbAction(level - playerNowOrbs);
        }
        return null;
    };

    public interface UTILS_PowerNeedReplacePlaceHolder {
        public Class<?> getReplaceClassType();
    };
    public static final class UTILS_PowerTargetPlaceHolder implements UTILS_PowerNeedReplacePlaceHolder {
        @Override
        public Class<?> getReplaceClassType() {
            return AbstractCreature.class;
        }
    };
    public static final UTILS_PowerTargetPlaceHolder UTILS_PowerTargetPlaceHolder = new UTILS_PowerTargetPlaceHolder();
    public static final class UTILS_PowerPlayerPlaceHolder implements UTILS_PowerNeedReplacePlaceHolder {
        @Override
        public Class<?> getReplaceClassType() {
            return AbstractCreature.class;
        }
    };
    public static final UTILS_PowerPlayerPlaceHolder UTILS_PowerPlayerPlaceHolder = new UTILS_PowerPlayerPlaceHolder();
    public static final class UTILS_PowerLevelPlaceHolder implements UTILS_PowerNeedReplacePlaceHolder {
        @Override
        public Class<?> getReplaceClassType() {
            return int.class;
        }
    };
    public static final UTILS_PowerLevelPlaceHolder UTILS_PowerLevelPlaceHolder = new UTILS_PowerLevelPlaceHolder();

    public static Object replaceArg(Object arg, int level, AbstractCreature target) {
        if (arg instanceof UTILS_PowerTargetPlaceHolder) {
            return target;
        } else if (arg instanceof UTILS_PowerLevelPlaceHolder) {
            return level;
        } else if (arg instanceof UTILS_PowerPlayerPlaceHolder) {
            return AbstractDungeon.player;
        }
        return null;
    }

    public static Object[] replaceArgs(Object[] args, int level, AbstractCreature target)  {
        Object[] result = new Object[args.length];
        for (int argIndex = 0; argIndex < args.length; argIndex++)  {
            Object nowArg = args[argIndex];
            Object resultArg = replaceArg(nowArg, level, target);
            if (resultArg != null) {
                result[argIndex] = resultArg;
            } else {
                result[argIndex] = nowArg;
            }
        }
        return result;
    }

    private static final Map<Class<?>, Class<?>> TypeReplaceMap = new HashMap<Class<?>, Class<?>>() {{
        put(Byte.class, byte.class);
        put(Short.class, short.class);
        put(Integer.class, int.class);
        put(Long.class, long.class);
        put(Float.class, float.class);
        put(Double.class, double.class);
        put(Character.class, char.class);
        put(Boolean.class, boolean.class);
    }};

    public static void checkParameters(Parameter[] parameters, Object[] args) {
        if (parameters.length != args.length) {
            throw new RuntimeException("Arguments length not match | C00");
        }
        for (int argIndex = 0; argIndex < args.length; argIndex++) {
            Object nowArg = args[argIndex];
            Class<?> parameterClass = parameters[argIndex].getType();
            if (nowArg instanceof UTILS_PowerNeedReplacePlaceHolder) {
                Class<?> replaceType = ((UTILS_PowerNeedReplacePlaceHolder) nowArg).getReplaceClassType();
                if (!parameterClass.isAssignableFrom(replaceType)) {
                    throw new RuntimeException("Argument [ " + ((Class<?>) replaceType).getName() +  " ] " + argIndex + " is not of type [ " + parameterClass.getName() + " ] | C01");
                }
            } else {
                Class<?> argType = nowArg.getClass();
                if (TypeReplaceMap.containsKey(argType)) {
                    argType = TypeReplaceMap.get(argType);
                }
                if (!parameterClass.isAssignableFrom(argType)) {
                    throw new RuntimeException("Argument [ " + nowArg.getClass().getName() + " ] " + argIndex + " is not of type [ " + parameters[argIndex].getType().getName() + " ] | C02");
                }
            }
        }
    }

    private static String getTab(int level) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < level; i++) {
            stringBuilder.append("\t");
        }
        return stringBuilder.toString();
    }

    private static void LogClassTypeArray(Class<?>[] classes, Consumer<String> logger, int Level) {
        for (int classesIndex = 0; classesIndex < classes.length; classesIndex++) {
            logger.accept(getTab(Level) + "Class [ " + classesIndex + " ] type: " + classes[classesIndex].getName());
        }
    }

    private static void LogAllParametersType(Constructor<?>[] constructors, Consumer<String> logger, int Level) {
        for (int constructorsIndex = 0; constructorsIndex < constructors.length; constructorsIndex++) {
            Constructor<?>constructor = constructors[constructorsIndex];
            logger.accept( getTab(Level) + "Constructor [ " + constructorsIndex + " ] parameters: ");
            Parameter[] parameters = constructor.getParameters();
            for (int parametersIndex = 0; parametersIndex < parameters.length; parametersIndex++) {
                logger.accept(getTab(Level + 1) + "Parameter [ " + parametersIndex + " ] type: " + parameters[parametersIndex].getType().getName());
            }
            logger.accept("");
        }
    }

    public static <T extends AbstractPower> BiFunction<AbstractCreature, Integer, AbstractGameAction> createApplyPowerAction(Class<T> powerClass, Class<?>[] constructorType, Object... args) {
        // 获取构造函数
        Constructor<?>[] constructors = powerClass.getDeclaredConstructors();
        Constructor<?> constructor = null;
        if (constructorType == null) {
            constructor = constructors[0];
            if (constructor.getParameterCount() != args.length) {
                throw new RuntimeException("Constructor parameter count does not match args length");
            }
        } else {
            for (Constructor<?> classConstructor : constructors) {
                Parameter[] parameters = classConstructor.getParameters();
                if (parameters.length == constructorType.length) {
                    boolean match = true;
                    for (int parametersIndex = 0; parametersIndex < parameters.length; parametersIndex++) {
                        Class<?> nowParametersType = parameters[parametersIndex].getType();
                        Class<?> nowConstructorType = constructorType[parametersIndex];
                        if (nowParametersType != nowConstructorType) {
                            XuCustomMod.LOGGER.warn("Parameter type [ " + nowParametersType.getName() + " ] does not match arg type [ " + nowConstructorType.getName() + " ]");
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        constructor = classConstructor;
                        break;
                    }
                }
            }
            if (constructor == null) {
                XuCustomMod.LOGGER.error("Failed to find constructor for power: {}", powerClass.getName());
                XuCustomMod.LOGGER.error("All constructors: ");
                LogAllParametersType(constructors, XuCustomMod.LOGGER::error, 1);
                XuCustomMod.LOGGER.error("Target constructor type: ");
                LogClassTypeArray(constructorType, XuCustomMod.LOGGER::error, 1);
                throw new RuntimeException("Failed to find constructor for power: " + powerClass.getName());
            }
        }
        // 测试参数类型是否匹配
        Parameter[] parameters = constructor.getParameters();
        checkParameters(parameters, args);
        Constructor<?> finalConstructor = constructor;
        return (target, level) -> {
            Object[] newArgs = replaceArgs(args, level, target);
            try {
                AbstractPower power = (AbstractPower) finalConstructor.newInstance(newArgs);
                return new ApplyPowerAction(target, target, power, level);
            } catch (Exception e) {
                XuCustomMod.LOGGER.error("Failed to create power: {}", powerClass.getName());
                return null;
            }
        };
    }

    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AccuracyPower = createApplyPowerAction(AccuracyPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 精准
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AfterImagePower = createApplyPowerAction(AfterImagePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 余像
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AmplifyPower = createApplyPowerAction(AmplifyPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 增幅
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AngerPower = createApplyPowerAction(AngerPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 激怒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AngryPower = createApplyPowerAction(AngryPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 生气
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ArtifactPower = createApplyPowerAction(ArtifactPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 人工制品
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_AttackBurnPower = createApplyPowerAction(AttackBurnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 攻击烧毁
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BarricadePower = createApplyPowerAction(BarricadePower.class, null, UTILS_PowerTargetPlaceHolder);  // 壁垒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BerserkPower = createApplyPowerAction(BerserkPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 狂暴
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BiasPower = createApplyPowerAction(BiasPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 偏差
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BlurPower = createApplyPowerAction(BlurPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 残影
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BrutalityPower = createApplyPowerAction(BrutalityPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 残暴
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BufferPower = createApplyPowerAction(BufferPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 缓冲
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BurstPower = createApplyPowerAction(BurstPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 爆发
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CollectPower = createApplyPowerAction(CollectPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 收集
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CombustPower = createApplyPowerAction(CombustPower.class, null, UTILS_PowerTargetPlaceHolder, 1, UTILS_PowerLevelPlaceHolder);  // 自燃
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ConfusionPower = createApplyPowerAction(ConfusionPower.class, null, UTILS_PowerTargetPlaceHolder);  // 混乱
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ConservePower = createApplyPowerAction(ConservePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 保留
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CorpseExplosionPower = createApplyPowerAction(CorpseExplosionPower.class, null, UTILS_PowerTargetPlaceHolder);  // 尸爆术
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CorruptionPower = createApplyPowerAction(CorruptionPower.class, null, UTILS_PowerTargetPlaceHolder);  // 腐化
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CreativeAIPower = createApplyPowerAction(CreativeAIPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 创造性AI
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CuriosityPower = createApplyPowerAction(CuriosityPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 好奇
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CurlUpPower = createApplyPowerAction(CurlUpPower.class, null, UTILS_PowerTargetPlaceHolder,  UTILS_PowerLevelPlaceHolder);  // 蜷身
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DarkEmbracePower = createApplyPowerAction(DarkEmbracePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 黑暗之拥
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DemonFormPower = createApplyPowerAction(DemonFormPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 恶魔形态
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DexterityPower = createApplyPowerAction(DexterityPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 敏捷
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DoubleDamagePower = createApplyPowerAction(DoubleDamagePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, false);  // 双倍伤害
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DoubleTapPower = createApplyPowerAction(DoubleTapPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 双发
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DrawCardNextTurnPower = createApplyPowerAction(DrawCardNextTurnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 抽牌
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DrawReductionPower = createApplyPowerAction(DrawReductionPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 抽牌减少
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DuplicationPower = createApplyPowerAction(DuplicationPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 复制
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EchoPower = createApplyPowerAction(EchoPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 回响形态
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ElectroPower = createApplyPowerAction(ElectroPower.class, null, UTILS_PowerTargetPlaceHolder);  // 电动力学
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EnergizedBluePower = createApplyPowerAction(EnergizedBluePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 能量提升
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EnergizedPower = createApplyPowerAction(EnergizedPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 能量提升
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EnvenomPower = createApplyPowerAction(EnvenomPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 涂毒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EquilibriumPower = createApplyPowerAction(EquilibriumPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 均衡
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EvolvePower = createApplyPowerAction(EvolvePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 进化
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ExplosivePower = createApplyPowerAction(ExplosivePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 爆炸
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FadingPower = createApplyPowerAction(FadingPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 消逝
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FeelNoPainPower = createApplyPowerAction(FeelNoPainPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 无惧疼痛
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FireBreathingPower = createApplyPowerAction(FireBreathingPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 火焰吐息
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FlameBarrierPower = createApplyPowerAction(FlameBarrierPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 火焰屏障
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FlightPower = createApplyPowerAction(FlightPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 飞行
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FocusPower = createApplyPowerAction(FocusPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 集中
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ForcefieldPower = createApplyPowerAction(ForcefieldPower.class, null, UTILS_PowerTargetPlaceHolder);  // 无视攻击
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FrailPower = createApplyPowerAction(FrailPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, false);  // 脆弱
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_GenericStrengthUpPower = createApplyPowerAction(GenericStrengthUpPower.class, null, UTILS_PowerTargetPlaceHolder, "NO_STRING", UTILS_PowerLevelPlaceHolder);  // NO_STRING
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_GrowthPower = createApplyPowerAction(GrowthPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 万物生长
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_HeatsinkPower = createApplyPowerAction(HeatsinkPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 散热
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_HelloPower = createApplyPowerAction(HelloPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 你好
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_InfiniteBladesPower = createApplyPowerAction(InfiniteBladesPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 无限刀刃
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_IntangiblePlayerPower = createApplyPowerAction(IntangiblePlayerPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 无实体
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_IntangiblePower = createApplyPowerAction(IntangiblePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 无实体
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_InvinciblePower = createApplyPowerAction(InvinciblePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 坚不可摧
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_JuggernautPower = createApplyPowerAction(JuggernautPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 势不可当
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_LightningMasteryPower = createApplyPowerAction(LightningMasteryPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // NO_STRING
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_LockOnPower = createApplyPowerAction(LockOnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 跟踪锁定
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_LoopPower = createApplyPowerAction(LoopPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 循环
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MagnetismPower = createApplyPowerAction(MagnetismPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 磁力
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MalleablePower = createApplyPowerAction(MalleablePower.class, new Class[]{AbstractCreature.class, int.class}, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 柔韧
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MayhemPower = createApplyPowerAction(MayhemPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 乱战
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MetallicizePower = createApplyPowerAction(MetallicizePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 金属化
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NextTurnBlockPower = createApplyPowerAction(NextTurnBlockPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, "NextTurnBlockPower");  // 下回合格挡
    // public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NightmarePower = createApplyPowerAction(NightmarePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 夜魇
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NoBlockPower = createApplyPowerAction(NoBlockPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, false);  // 无法格挡
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NoDrawPower = createApplyPowerAction(NoDrawPower.class, null, UTILS_PowerTargetPlaceHolder);  // 无法抽牌
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NoxiousFumesPower = createApplyPowerAction(NoxiousFumesPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 毒雾
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_PanachePower = createApplyPowerAction(PanachePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 神气制胜
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_PenNibPower = createApplyPowerAction(PenNibPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 钢笔尖
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_PhantasmalPower = createApplyPowerAction(PhantasmalPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 幻影
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_PlatedArmorPower = createApplyPowerAction(PlatedArmorPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 多层护甲
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_PoisonPower = createApplyPowerAction(PoisonPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerPlayerPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 中毒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RagePower = createApplyPowerAction(RagePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 愤怒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ReboundPower = createApplyPowerAction(ReboundPower.class, null, UTILS_PowerTargetPlaceHolder);  // 弹回
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RechargingCorePower = createApplyPowerAction(RechargingCorePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // NO_STRING
    // public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RegenerateMonsterPower = createApplyPowerAction(RegenerateMonsterPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 再生
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RegenPower = createApplyPowerAction(RegenPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 再生
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RepairPower = createApplyPowerAction(RepairPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 修理
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RetainCardPower = createApplyPowerAction(RetainCardPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 保留卡牌
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RitualPower = createApplyPowerAction(RitualPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, true);  // 仪式
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RupturePower = createApplyPowerAction(RupturePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 撕裂
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_SadisticPower = createApplyPowerAction(SadisticPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 残虐
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ShiftingPower = createApplyPowerAction(ShiftingPower.class, null, UTILS_PowerTargetPlaceHolder);  // 变化
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_SkillBurnPower = createApplyPowerAction(SkillBurnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 技能烧毁
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_SlowPower = createApplyPowerAction(SlowPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 缓慢
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_SporeCloudPower = createApplyPowerAction(SporeCloudPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 孢子云
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_StaticDischargePower = createApplyPowerAction(StaticDischargePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 静电释放
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_StormPower = createApplyPowerAction(StormPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 雷暴
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_StrengthPower = createApplyPowerAction(StrengthPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 力量
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_StrikeUpPower = createApplyPowerAction(StrikeUpPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 打击提升
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn1 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 1, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:1
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn2 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 2, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:2
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn3 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 3, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:3
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn4 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 4, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:4
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn5 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 5, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:5
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn6 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 6, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:6
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn7 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 7, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:7
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn8 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 8, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:8
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn9 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 9, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:9
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_TheBombPowerTurn10 = createApplyPowerAction(TheBombPower.class, null, UTILS_PowerTargetPlaceHolder, 10, UTILS_PowerLevelPlaceHolder);  // 炸弹 Turn:10
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ThornsPower = createApplyPowerAction(ThornsPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 荆棘
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ThousandCutsPower = createApplyPowerAction(ThousandCutsPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 凌迟
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ToolsOfTheTradePower = createApplyPowerAction(ToolsOfTheTradePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 必备工具
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_VulnerablePower = createApplyPowerAction(VulnerablePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, false);  // 易伤
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_WeakPower = createApplyPowerAction(WeakPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, false);  // 虚弱
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_WinterPower = createApplyPowerAction(WinterPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // NO_STRING
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_WraithFormPower = createApplyPowerAction(WraithFormPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 幽魂形态
    // watcher
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BattleHymnPower = createApplyPowerAction(BattleHymnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 战歌
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_BlockReturnPower = createApplyPowerAction(BlockReturnPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 格挡返还
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_CannotChangeStancePower = createApplyPowerAction(CannotChangeStancePower.class, null, UTILS_PowerTargetPlaceHolder);  // 不能改变姿态
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DevaPower = createApplyPowerAction(DevaPower.class, null, UTILS_PowerTargetPlaceHolder);  // 天人
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_DevotionPower = createApplyPowerAction(DevotionPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 虔信
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EndTurnDeathPower = createApplyPowerAction(EndTurnDeathPower.class, null, UTILS_PowerTargetPlaceHolder);  // 渎神者
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EnergyDownPower = createApplyPowerAction(EnergyDownPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder, true);  // 斋戒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_EstablishmentPower = createApplyPowerAction(EstablishmentPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 确立基础
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_ForesightPower = createApplyPowerAction(ForesightPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 先见之明
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_FreeAttackPower = createApplyPowerAction(FreeAttackPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 免费攻击
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_LikeWaterPower = createApplyPowerAction(LikeWaterPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 如水
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_LiveForeverPower = createApplyPowerAction(LiveForeverPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 永生不死
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MantraPower = createApplyPowerAction(MantraPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 真言
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MarkPower = createApplyPowerAction(MarkPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 印记
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MasterRealityPower = createApplyPowerAction(MasterRealityPower.class, null, UTILS_PowerTargetPlaceHolder);  // 操控现实
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_MentalFortressPower = createApplyPowerAction(MentalFortressPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 心灵堡垒
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NirvanaPower = createApplyPowerAction(NirvanaPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 涅槃
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_NoSkillsPower = createApplyPowerAction(NoSkillsPower.class, null, UTILS_PowerTargetPlaceHolder);  // 禁用技能
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_OmegaPower = createApplyPowerAction(OmegaPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 欧米伽
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_OmnisciencePower = createApplyPowerAction(OmnisciencePower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 通晓万物
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_RushdownPower = createApplyPowerAction(RushdownPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 猛虎下山
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_StudyPower = createApplyPowerAction(StudyPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 研习
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_VigorPower = createApplyPowerAction(VigorPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 活力
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_WaveOfTheHandPower = createApplyPowerAction(WaveOfTheHandPower.class, null, UTILS_PowerTargetPlaceHolder, UTILS_PowerLevelPlaceHolder);  // 摆手
    public static final BiFunction<AbstractCreature, Integer, AbstractGameAction> P_WrathNextTurnPower = createApplyPowerAction(WrathNextTurnPower.class, null, UTILS_PowerTargetPlaceHolder);  // 怒火中烧
}
