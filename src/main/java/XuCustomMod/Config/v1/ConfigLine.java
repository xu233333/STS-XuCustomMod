package XuCustomMod.Config.v1;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigLine <T> {

    public final String Key;
    public final T DefaultValue;
    public T Value;
    public final Supplier<T> ValueGetter;
    public final Consumer<T> ValueSetter;
    public final Boolean UseGetterValueAsDefault;
    public final T GetterValueDefault;

    public final ConfigValueType<T> ValueType;

    public ConfigLine(String key, T defaultValue, Supplier<T> valueGetter, Consumer<T> valueSetter, Boolean UseGetterValueAsDefault) {
        this.Key = key;
        this.DefaultValue = defaultValue;
        this.ValueGetter = valueGetter;
        this.ValueSetter = valueSetter;
        this.ValueType = (ConfigValueType<T>) ConfigValueType.getInstance(defaultValue.getClass());
        this.UseGetterValueAsDefault = UseGetterValueAsDefault;
        if (this.UseGetterValueAsDefault) {
            this.GetterValueDefault = valueGetter.get();
        } else {
            this.GetterValueDefault = null;
        }
        if (this.ValueType == null) {
            throw new RuntimeException("ConfigValueType not found for " + defaultValue.getClass());
        }
    }

    public ConfigLine(String key, T defaultValue, Supplier<T> valueGetter, Consumer<T> valueSetter) {
        this(key, defaultValue, valueGetter, valueSetter, false);
    }

    public T getDefaultValue() {
        if (UseGetterValueAsDefault) {
            return this.GetterValueDefault;
        } else {
            return this.DefaultValue;
        }
    }

    public void setDefaultProperty(Properties property) {
        property.setProperty(this.Key, String.valueOf(this.getDefaultValue()));
    }

    public void loadValue(SpireConfig config) {
        this.Value = this.ValueType.get(config, this.Key);
        this.ValueSetter.accept(this.Value);
    }

    public void saveValue(SpireConfig config) {
        this.Value = this.ValueGetter.get();
        if (this.Value != null) {
            this.ValueType.set(config, this.Key, this.Value);
        }
    }

    public void applyDefaultValue() {
        this.ValueSetter.accept(this.getDefaultValue());
    }

    public static Properties loadDefaultProperties(List<ConfigLine<?>> configLines) {
        Properties property = new Properties();
        for (ConfigLine<?> configLine : configLines) {
            configLine.setDefaultProperty(property);
        }
        return property;
    }

    public static void loadValues(SpireConfig config, List<ConfigLine<?>> configLines) {
        for (ConfigLine<?> configLine : configLines) {
            configLine.loadValue(config);
        }
    }

    public static void saveValues(SpireConfig config, List<ConfigLine<?>> configLines) {
        for (ConfigLine<?> configLine : configLines) {
            configLine.saveValue(config);
        }
    }

    public static void applyDefaultValues(List<ConfigLine<?>> configLines) {
        for (ConfigLine<?> configLine : configLines) {
            configLine.applyDefaultValue();
        }
    }
}
