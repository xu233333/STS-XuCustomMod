package XuCustomMod.Relic;

import XuCustomMod.Config.CommonConfig;
import XuCustomMod.Utils.ActionRegistries;
import XuCustomMod.Utils.ImageCategoryEnum;
import XuCustomMod.Utils.JsonUtils;
import XuCustomMod.Utils.LeveledAction;
import XuCustomMod.XuCustomMod;
import basemod.Pair;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavableRaw;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Relic_GodLike extends CustomRelic implements CustomSavableRaw {
    public static final String IN_MOD_ID = "GodLike";
    public static final String ID = XuCustomMod.makePath(IN_MOD_ID);

    public static final LeveledAction[] BattleStartPowers = {
            new LeveledAction(ActionRegistries.P_StrengthPower, false, false, 0, 50, 0),
            new LeveledAction(ActionRegistries.P_DexterityPower, false, false, 0, 50, 0),
            new LeveledAction(ActionRegistries.P_FocusPower, false, false, 0, 25, 0),
            new LeveledAction(ActionRegistries.P_ArtifactPower, false, false, 0, 20, 20),
            new LeveledAction(ActionRegistries.P_BarricadePower, false, false, 0, 1, 1),
            new LeveledAction(ActionRegistries.P_ThornsPower, false, false, 0, 10, 0),
            new LeveledAction(ActionRegistries.P_PlatedArmorPower, false, false, 0, 100, 20),
            new LeveledAction(ActionRegistries.P_BufferPower, false, false, 0, 50, 10),
            new LeveledAction(ActionRegistries.P_MetallicizePower, false, false, 0, 50, 0),
            new LeveledAction(ActionRegistries.P_RegenPower, false, false, 0, 50, 10),
            new LeveledAction(ActionRegistries.P_EnvenomPower, false, false, 0, 10, 0),
            new LeveledAction(ActionRegistries.P_RupturePower, false, false, 0, 10, 0),
            new LeveledAction(ActionRegistries.P_AfterImagePower, false, false, 0, 10, 0),
            new LeveledAction(ActionRegistries.P_RitualPower, false, false, 0, 10, 2),
            new LeveledAction(ActionRegistries.P_LiveForeverPower, false, false, 0, 10, 2),
            new LeveledAction(ActionRegistries.P_MalleablePower, false, false, 0, 50, 10),
            new LeveledAction(ActionRegistries.GainEnergyAction, false, false, 0, 10, 10),
    };

    public static final LeveledAction[] TurnStartPowers = {
            new LeveledAction(ActionRegistries.P_StrengthPower, false, false, 0, 10, 0),
            new LeveledAction(ActionRegistries.P_DexterityPower, false, false,0, 10, 0),
            new LeveledAction(ActionRegistries.P_FocusPower, false, false,0, 5, 0),
            new LeveledAction(ActionRegistries.P_RitualPower, false, false, 0, 1, 1),
            new LeveledAction(ActionRegistries.P_LiveForeverPower, false, false, 0, 1, 1),
            new LeveledAction(ActionRegistries.GainEnergyAction, false, false, 0, 10, 10),
            new LeveledAction(ActionRegistries.IncreaseMaxOrbActionWhenPlayerHasOrb, false, false, 0, 10, 10)
    };

    private final Consumer<AbstractGameAction> applyAction = this::addToBot;

    public int MinOrbCount = 10;
    public int StartOrbCount = 10;
    public int ExtraEnergy = 5;

    private boolean IsEnable = CommonConfig.Instance.RelicDefaultEnableState;
    private boolean ThisBattleEnable = false;
    private boolean ThisTurnEnable = false;
    private int EnableLevel = this.IsEnable ? 1 : 0;  // 0 -> off 1 -> Full 2 -> Lite

    private String[] Desc = null;
    private String[] ExtraMsg = null;

    public Relic_GodLike() {
        super(ID, new Texture(Gdx.files.internal(ImageCategoryEnum.getImagePath(ImageCategoryEnum.RELICS, IN_MOD_ID))), RelicTier.STARTER, LandingSound.FLAT);
        this.UpdateCounter();
    }

    public void ApplyBattleStartEffect() {
        if (AbstractDungeon.player == null) {
            return;
        }
        this.ThisBattleEnable = true;
        try {
            LeveledAction.applyAll(applyAction, AbstractDungeon.player, this.EnableLevel, BattleStartPowers);
        } catch (Exception e) {
            XuCustomMod.LOGGER.info("Relic_GodLike ApplyBattleStartEffect Error: {}", e.getMessage());
        }
    }

    public void ApplyTurnStartEffect() {
        if (AbstractDungeon.player == null) {
            return;
        }
        this.ThisTurnEnable = true;
        try {
            LeveledAction.applyAll(applyAction, AbstractDungeon.player, this.EnableLevel, TurnStartPowers);
        } catch (Exception e) {
            XuCustomMod.LOGGER.info("Relic_GodLike ApplyTurnStartEffect Error: {}", e.getMessage());
        }
    }

    /* 有Bug // "你获得永久 #b5 点 [E] 。",
    @Override
    public void onEquip() {
        AbstractDungeon.player.energy.energyMaster += ExtraEnergy;
    }

    @Override
    public void onUnequip() {
        AbstractDungeon.player.energy.energyMaster -= ExtraEnergy;
    }
     */

    
    @Override
    public void atBattleStart() {
        super.atBattleStart();
        this.ThisBattleEnable = false;
        if (this.IsEnable) {
            this.ApplyBattleStartEffect();
        }
    }

    @Override
    public void atTurnStart() {
        super.atTurnStart();
        this.ThisTurnEnable = false;
        if (this.IsEnable) {
            this.ApplyTurnStartEffect();
        }
    }

    @Override
    public String getUpdatedDescription() {
        StringBuilder FinalDesc = new StringBuilder();
        if (this.Desc == null) {
            this.loadDesc();
        }
        for (String Desc : this.Desc) {
            FinalDesc.append(Desc).append(" ");
        }
        return FinalDesc.toString();
    }

    public void loadDesc() {
        List<String> DescBuilder = new ArrayList<>();
        List<String> ExtraMSGBuilder = new ArrayList<>();
        boolean DescFinish = false;
        boolean ExtraMSGFinish = false;
        for (String string : this.DESCRIPTIONS) {
            if (!DescFinish) {
                if (string.isEmpty()) {
                    DescFinish = true;
                } else {
                    DescBuilder.add(string);
                }
                continue;
            }
            if (!ExtraMSGFinish) {
                if (string.isEmpty()) {
                    ExtraMSGFinish = true;
                } else {
                    ExtraMSGBuilder.add(string);
                }
                continue;
            }
            break;
        }
        this.Desc = DescBuilder.toArray(new String[0]);
        this.ExtraMsg = ExtraMSGBuilder.toArray(new String[0]);
    }

    @Override
    public void update() {
        super.update();
        if (this.hb != null && this.hb.hovered && InputHelper.justClickedRight) {
            this.OnRightClick();
        }
    }

    public void OnRightClick() {
        int Level = (this.EnableLevel + 1) % 3;
        this.SetEnableLevel(Level);
        this.UpdateEnable();
    }

    public void UpdateEnable() {
        if (this.IsEnable) {
            if (!this.ThisBattleEnable) {
                this.ApplyBattleStartEffect();
            }
            if (!this.ThisTurnEnable) {
                this.ApplyTurnStartEffect();
            }
        }
    }

    public void UpdateCounter() {
        if (this.IsEnable) {
            this.counter = this.EnableLevel;
        } else {
            this.counter = -1;
        }
    }

    public void SetEnableLevel(int Level) {
        if (Level != this.EnableLevel) {
            if (AbstractDungeon.player == null) {
                return;
            }
            if (this.ExtraMsg == null) {
                this.loadDesc();
            }
            AbstractDungeon.effectList.add(new ThoughtBubble(AbstractDungeon.player.dialogX, AbstractDungeon.player.dialogY, 3.0F, this.ExtraMsg[Level], true));
        }
        this.IsEnable = Level > 0;
        this.EnableLevel = Level;
        this.UpdateCounter();
    }

    @Override
    public AbstractRelic makeCopy() {
        return new Relic_GodLike();
    }

    public JsonElement onSaveRaw() {
        return JsonUtils.createJsonObject(
                new Pair<>("IsEnable", this.IsEnable),
                new Pair<>("EnableLevel", this.EnableLevel),
                new Pair<>("ThisBattleEnable", this.ThisBattleEnable),
                new Pair<>("ThisTurnEnable", this.ThisTurnEnable)
        );
    }

    public void onLoadRaw(JsonElement value) {
        JsonObject json = null;
        if (value instanceof JsonObject) {
            json = (JsonObject) value;
        } else {
            json = new JsonObject();
        }
        this.IsEnable = JsonUtils.getOrDefault(json, "IsEnable", this.IsEnable);
        this.SetEnableLevel(JsonUtils.getOrDefault(json, "EnableLevel", this.EnableLevel));
        this.ThisBattleEnable = JsonUtils.getOrDefault(json, "ThisBattleEnable", this.ThisBattleEnable);
        this.ThisTurnEnable = JsonUtils.getOrDefault(json, "ThisTurnEnable", this.ThisTurnEnable);
    }
}
