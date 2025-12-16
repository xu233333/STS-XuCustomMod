package XuCustomMod;

import XuCustomMod.Config.CommonConfig;
import XuCustomMod.Config.ConfigRegistry;
import XuCustomMod.Relic.Relic_GodLike;
import basemod.BaseMod;
import basemod.helpers.RelicType;
import basemod.interfaces.*;
import com.badlogic.gdx.Gdx;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static basemod.BaseMod.gson;

@SpireInitializer
public class XuCustomMod implements EditRelicsSubscriber, StartGameSubscriber, OnStartBattleSubscriber, EditStringsSubscriber, EditKeywordsSubscriber, PostInitializeSubscriber {
    public static final String MOD_ID = "XuCustomMod";
    public static final String MOD_RES_ID = "XuCustomMod_RES";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String KEYWORD_KEY = "Game Dictionary";

    public static final Settings.GameLanguage DefaultLang = Settings.GameLanguage.ZHS;
    public static final List<Settings.GameLanguage> SupportedLang = new ArrayList<Settings.GameLanguage>() {{
        this.add(Settings.GameLanguage.ZHS);
    }};

    public static Settings.GameLanguage getAvailableLang() {
        if (SupportedLang.contains(Settings.language)) {
            return Settings.language;
        } else {
            return DefaultLang;
        }
    }

    public static String makePath(String path) {
        return MOD_ID + ":" + path;
    }

    public XuCustomMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new XuCustomMod();
    }

    public void giveRelic() {
        if (AbstractDungeon.player != null) {
            if (!AbstractDungeon.player.hasRelic(Relic_GodLike.ID)) {
                AbstractDungeon.player.relics.add(new Relic_GodLike());
                AbstractDungeon.player.reorganizeRelics();
            }
        }
    }

    @Override
    public void receiveStartGame() {
        if (CommonConfig.Instance.GiveRelicOnStart) {
            this.giveRelic();
        }
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom abstractRoom) {
        if (CommonConfig.Instance.GiveRelicOnBattleStart) {
            this.giveRelic();
        }
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelic(new Relic_GodLike(), RelicType.SHARED);
    }

    @Override
    public void receiveEditStrings() {
        String lang = getAvailableLang().toString();
        BaseMod.loadCustomStringsFile(RelicStrings.class, MOD_RES_ID + "/localization/" + lang + "/relics.json");
    }

    @Override
    public void receiveEditKeywords() {
        try {
            String lang = getAvailableLang().toString();
            String KeyWordFilePath = MOD_RES_ID + "/localization/" + lang + "/keywords.json";
            String json = Gdx.files.internal(KeyWordFilePath)
                    .readString(String.valueOf(StandardCharsets.UTF_8));
            JsonObject keywordsObject = gson.fromJson(json, JsonObject.class);
            if (keywordsObject != null && keywordsObject.has(KEYWORD_KEY)) {
                JsonObject keywordObject = keywordsObject.get(KEYWORD_KEY).getAsJsonObject();
                for (Map.Entry<String, JsonElement> keyword : keywordObject.entrySet()) {
                    JsonObject keywordData = keyword.getValue().getAsJsonObject();
                    if ((keywordData.has("NAMES") && keywordData.get("NAMES").isJsonArray() && keywordData.get("NAMES").getAsJsonArray().size() != 0) && (keywordData.has("DESCRIPTION") && keywordData.get("DESCRIPTION").isJsonPrimitive())) {
                        JsonArray NAMES = keywordData.get("NAMES").getAsJsonArray();
                        String DESCRIPTION = keywordData.get("DESCRIPTION").getAsString();
                        List<String> NAME_String_List = new LinkedList<>();
                        for (JsonElement name : NAMES) {
                            if (name.isJsonPrimitive()) {
                                NAME_String_List.add(name.getAsString());
                            }
                        }
                        BaseMod.addKeyword(MOD_ID, keyword.getKey(), NAME_String_List.toArray(new String[]{}), DESCRIPTION);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error loading keywords", e);
        }
    }

    @Override
    public void receivePostInitialize() {
        try {
            ConfigRegistry.loadAllConfigs();
        } catch (Exception e) {
            LOGGER.error("Error loading config", e);
        }
    }
}
