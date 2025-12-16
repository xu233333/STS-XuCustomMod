package XuCustomMod.Config.v1;

import XuCustomMod.Config.IConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public abstract class AbstractConfig implements IConfig {
    public AbstractConfig(Boolean ApplyDefaultValues) {
        if (ApplyDefaultValues) {
            this.applyDefaultValues();
        }
    }

    public SpireConfig getConfig() throws IOException {
        return new SpireConfig(this.getConfigNameSpace(), this.getConfigPath(), this.getDefaultProperties());
    }

    public boolean isGeneratedInitConfigLines = false;
    public boolean isGeneratedConfigLines = false;
    public ConfigLine<Boolean> initConfigLine;
    public List<ConfigLine<?>> configLines;

    public abstract ConfigLine<Boolean> generateInitConfigLine();
    public abstract List<ConfigLine<?>> generateConfigLines();
    public abstract String getConfigNameSpace();
    public abstract String getConfigPath();

    public ConfigLine<Boolean> getInitConfigLineFromCache() {
        if (!isGeneratedInitConfigLines) {
            this.initConfigLine = generateInitConfigLine();
            this.isGeneratedInitConfigLines = true;
        }
        return this.initConfigLine;
    }

    public List<ConfigLine<?>> getConfigLinesFromCache() {
        if (!isGeneratedConfigLines) {
            this.configLines = generateConfigLines();
            this.configLines.add(getInitConfigLineFromCache());
            this.isGeneratedConfigLines = true;
        }
        return this.configLines;
    }

    public boolean isInitialized() {
        return this.getInitConfigLineFromCache().ValueGetter.get();
    }

    public void setInitialized() {
        this.getInitConfigLineFromCache().ValueSetter.accept(true);
    }

    public Properties getDefaultProperties() {
        return ConfigLine.loadDefaultProperties(this.getConfigLinesFromCache());
    }

    public void applyDefaultValues() {
        ConfigLine.applyDefaultValues(this.getConfigLinesFromCache());
    }

    @Override
    public void save() {
        try {
            SpireConfig config = this.getConfig();
            this.setInitialized();
            ConfigLine.saveValues(config, this.getConfigLinesFromCache());
            config.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load() {
        try {
            SpireConfig config = this.getConfig();
            ConfigLine.loadValues(config, this.getConfigLinesFromCache());
            if (!isInitialized()) {
                this.save();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
