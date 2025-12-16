package XuCustomMod.Config;

import XuCustomMod.Config.v2.AbstractJsonConfig;
import XuCustomMod.Utils.JsonUtils;
import XuCustomMod.XuCustomMod;
import basemod.Pair;
import com.google.gson.JsonObject;

public class CommonConfig extends AbstractJsonConfig {
    public static CommonConfig Instance = new CommonConfig();

    public boolean RelicDefaultEnableState = true;
    public boolean GiveRelicOnStart = true;
    public boolean GiveRelicOnBattleStart = true;

    public CommonConfig() {
        super(XuCustomMod.MOD_ID, "common_config");
    }

    @Override
    public void loadFormJson(JsonObject jsonObject) {
        this.RelicDefaultEnableState = JsonUtils.getOrDefault(jsonObject, "RelicDefaultEnableState", true);
        this.GiveRelicOnStart = JsonUtils.getOrDefault(jsonObject, "GiveRelicOnStart", true);
        this.GiveRelicOnBattleStart = JsonUtils.getOrDefault(jsonObject, "GiveRelicOnBattleStart", true);
    }

    @Override
    public JsonObject saveToJson() {
        return JsonUtils.createJsonObject(
                new Pair<>("RelicDefaultEnableState", this.RelicDefaultEnableState),
                new Pair<>("GiveRelicOnStart", this.GiveRelicOnStart),
                new Pair<>("GiveRelicOnBattleStart", this.GiveRelicOnBattleStart)
        );
    }
}
