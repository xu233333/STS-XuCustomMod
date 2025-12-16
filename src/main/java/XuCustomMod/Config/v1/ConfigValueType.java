package XuCustomMod.Config.v1;


import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ConfigValueType <T> {
    public static List<ConfigValueType<?>> ConfigValueTypes = new ArrayList<ConfigValueType<?>>() {{
        this.add(ConfigValueType.of(String.class, SpireConfig::getString, (config, key) -> {return (value) -> {config.setString(key, value);};}));
        this.add(ConfigValueType.of(Boolean.class, SpireConfig::getBool, (config, key) -> {return (value) -> {config.setBool(key, value);};}));
        this.add(ConfigValueType.of(Integer.class, SpireConfig::getInt, (config, key) -> {return (value) -> {config.setInt(key, value);};}));
        this.add(ConfigValueType.of(Float.class, SpireConfig::getFloat, (config, key) -> {return (value) -> {config.setFloat(key, value);};}));
    }};

    public final Class<T> TypeClass;
    public final BiFunction<SpireConfig, String, T> ConfigGetter;
    public final  BiFunction<SpireConfig, String, Consumer<T>> ConfigSetter;

    public ConfigValueType(Class<T> typeClass, BiFunction<SpireConfig, String, T> configGetter, BiFunction<SpireConfig, String, Consumer<T>> configSetter) {
        TypeClass = typeClass;
        ConfigGetter = configGetter;
        ConfigSetter = configSetter;
    }

    public T get(SpireConfig config, String key) {
        return ConfigGetter.apply(config, key);
    }

    public void set(SpireConfig config, String Key, T value) {
        ConfigSetter.apply(config, Key).accept(value);
    }

    public static <T> ConfigValueType<T> of(Class<T> typeClass, BiFunction<SpireConfig, String, T> configGetter, BiFunction<SpireConfig, String, Consumer<T>> configSetter) {
        return new ConfigValueType<>(typeClass, configGetter, configSetter);
    }

    public static <T> ConfigValueType<T> getInstance(Class<T> typeClass) {
        for (ConfigValueType<?> configValueType : ConfigValueTypes) {
            if (configValueType.TypeClass == typeClass) {
                return (ConfigValueType<T>) configValueType;
            }
        }
        return null;
    }
}
