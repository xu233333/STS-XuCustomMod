package XuCustomMod.Relic;

import XuCustomMod.Config.CommonConfig;
import XuCustomMod.Utils.ImageCategoryEnum;
import XuCustomMod.Utils.JsonUtils;
import XuCustomMod.XuCustomMod;
import basemod.Pair;
import basemod.abstracts.CustomRelic;
import basemod.abstracts.CustomSavableRaw;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.defect.IncreaseMaxOrbAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.*;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.vfx.ThoughtBubble;

import java.util.ArrayList;
import java.util.List;

public class Relic_GodLike extends CustomRelic implements CustomSavableRaw {
    public static final String IN_MOD_ID = "GodLike";
    public static final String ID = XuCustomMod.makePath(IN_MOD_ID);

    public int StrengthLevel = 50;
    public int StrengthLevelPerTurn = 10;
    public int DexterityLevel = 50;
    public int DexterityLevelPerTurn = 10;
    public int FocusLevel = 25;
    public int FocusLevelPerTurn = 5;
    public int ArtifactLevel = 20;
    public int ThornsLevel = 10;
    public int PlatedArmorLevel = 200;
    public int BufferLevelLite = 10;
    public int BufferLevelFullExtra = 40;
    public int MetallicizeLevel = 50;
    public int RegenLevelLite = 10;
    public int RegenLevelFullExtra = 40;
    public int EnvenomLevel = 10;
    public int RuptureLevel = 10;
    public int AfterImageLevel = 10;
    public int RitualPowerLevelLite = 2;
    public int RitualPowerLevelFullExtra = 6;
    public int RitualPowerLevelPerTurn = 1;

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
            // Lite
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new ArtifactPower(AbstractDungeon.player, ArtifactLevel), ArtifactLevel));  // 人工制品
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new BarricadePower(AbstractDungeon.player), 1));  // 壁垒
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RegenPower(AbstractDungeon.player, RegenLevelLite), RegenLevelLite));  // 再生
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new BufferPower(AbstractDungeon.player, BufferLevelLite), BufferLevelLite));  // 缓冲
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RitualPower(AbstractDungeon.player, RitualPowerLevelLite, true), RitualPowerLevelLite));  // 仪式
            this.addToBot(new GainEnergyAction(10));

            // Full
            if (this.EnableLevel == 1) {
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, StrengthLevel), StrengthLevel));  // 力量
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new DexterityPower(AbstractDungeon.player, DexterityLevel), DexterityLevel));  // 敏捷
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new FocusPower(AbstractDungeon.player, FocusLevel), FocusLevel));  // 集中
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new ThornsPower(AbstractDungeon.player, ThornsLevel), ThornsLevel));  // 荆棘
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new PlatedArmorPower(AbstractDungeon.player, PlatedArmorLevel), PlatedArmorLevel));  // 多层护甲
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new MetallicizePower(AbstractDungeon.player, MetallicizeLevel), MetallicizeLevel));  // 金属化
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RegenPower(AbstractDungeon.player, RegenLevelFullExtra), RegenLevelFullExtra));  // 再生
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new BufferPower(AbstractDungeon.player, BufferLevelFullExtra), BufferLevelFullExtra));  // 缓冲
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new EnvenomPower(AbstractDungeon.player, EnvenomLevel), EnvenomLevel));  // 涂毒
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RupturePower(AbstractDungeon.player, RuptureLevel), RuptureLevel));  // 撕裂
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new AfterImagePower(AbstractDungeon.player, AfterImageLevel), AfterImageLevel));  // 余像
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RitualPower(AbstractDungeon.player, RitualPowerLevelFullExtra, true), RitualPowerLevelFullExtra));  // 仪式

                if (AbstractDungeon.player.maxOrbs > 0) {
                    this.addToBot(new IncreaseMaxOrbAction(StartOrbCount));
                }
            }
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
            // Lite
            this.addToBot(new GainEnergyAction(10));
            this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new RitualPower(AbstractDungeon.player, RitualPowerLevelPerTurn, true), RitualPowerLevelPerTurn));  // 仪式

            // Full
            if (this.EnableLevel == 1) {
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new StrengthPower(AbstractDungeon.player, StrengthLevelPerTurn), StrengthLevelPerTurn));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new DexterityPower(AbstractDungeon.player, DexterityLevelPerTurn), DexterityLevelPerTurn));
                this.addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new FocusPower(AbstractDungeon.player, FocusLevelPerTurn), FocusLevelPerTurn));
                this.setMaxOrbCount(MinOrbCount);
            }
        } catch (Exception e) {
            XuCustomMod.LOGGER.info("Relic_GodLike ApplyTurnStartEffect Error: {}", e.getMessage());
        }
    }

    private void setMaxOrbCount(int count) {
        if (AbstractDungeon.player.maxOrbs > 0) {
            this.addToBot(new IncreaseMaxOrbAction(count - AbstractDungeon.player.maxOrbs));
        }
    }

    @Override
    public void onUseCard(AbstractCard targetCard, UseCardAction useCardAction) {
        if (this.IsEnable) {
            this.setMaxOrbCount(MinOrbCount);
        }
    }

    @Override
    public void onPlayCard(AbstractCard c, AbstractMonster m) {
        if (this.IsEnable) {
            this.setMaxOrbCount(MinOrbCount);
        }
    }

    @Override
    public void onEquip() {
        AbstractDungeon.player.energy.energyMaster += ExtraEnergy;
    }

    @Override
    public void onUnequip() {
        AbstractDungeon.player.energy.energyMaster -= ExtraEnergy;
    }

    
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
