package XuCustomMod.Utils;

import XuCustomMod.XuCustomMod;

public enum KeyWordEnum {
    RELIC_ENABLE(XuCustomMod.makePath("RelicEnabled")),
    RELIC_DISABLE(XuCustomMod.makePath("RelicDisabled"));

    private String name;

    KeyWordEnum(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getName(boolean UseExtraLetter) {
        if (UseExtraLetter) {
            return "#y" + this.getName();
        }
        return this.getName();
    }
}
