package XuCustomMod.Config;

import XuCustomMod.Config.v1.AbstractConfig;
import XuCustomMod.Config.v1.ConfigLine;
import XuCustomMod.XuCustomMod;

import java.util.ArrayList;
import java.util.List;

public class ToolsConfig extends AbstractConfig {
    public static ToolsConfig Instance = new ToolsConfig();

    public boolean IsInit = false;

    public ToolsConfig() {
        super(false);
    }

    @Override
    public ConfigLine<Boolean> generateInitConfigLine() {
        return new ConfigLine<Boolean>("!IsInit!", false,  () -> { return this.IsInit; }, (data) -> { this.IsInit = data; });
    }

    @Override
    public List<ConfigLine<?>> generateConfigLines() {
        ToolsConfig RealThis = this;
        return new ArrayList<ConfigLine<?>>();
    }

    @Override
    public String getConfigNameSpace() {
        return XuCustomMod.MOD_ID;
    }

    @Override
    public String getConfigPath() {
        return "tools_config";
    }
}
