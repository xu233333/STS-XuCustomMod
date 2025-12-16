package XuCustomMod.Config;

import XuCustomMod.XuCustomMod;
import basemod.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ConfigRegistry {
    public static Map<Pair<String, String>, IConfig> configRegistry = new HashMap<>();
    public static Map<Pair<String, String>, List<Consumer<IConfig>>> configBeforeLoadListeners = new HashMap<>();
    public static Map<Pair<String, String>, List<Consumer<IConfig>>> configAfterLoadListeners = new HashMap<>();
    public static Map<Pair<String, String>, List<Consumer<IConfig>>> configBeforeSaveListeners = new HashMap<>();
    public static Map<Pair<String, String>, List<Consumer<IConfig>>> configAfterSaveListeners = new HashMap<>();

    public static final Pair<String, String> CONFIG_ToolsConfig = registerConfig(new Pair<>(XuCustomMod.MOD_ID, "XuCustomMod_Config"), ToolsConfig.Instance);
    public static final Pair<String, String> CONFIG_CommonConfig = registerConfig(new Pair<>(XuCustomMod.MOD_ID, "Common_Config"), CommonConfig.Instance);

    public static Pair<String, String> registerConfig(Pair<String, String> Identifier, IConfig config) {
        configRegistry.put(Identifier, config);
        configBeforeLoadListeners.put(Identifier, new java.util.ArrayList<>());
        configAfterLoadListeners.put(Identifier, new java.util.ArrayList<>());
        configBeforeSaveListeners.put(Identifier, new java.util.ArrayList<>());
        configAfterSaveListeners.put(Identifier, new java.util.ArrayList<>());
        return Identifier;
    }

    public static void registerConfigAfterLoadListener(Pair<String, String> Identifier, Consumer<IConfig> listener) {
        if (!configAfterLoadListeners.containsKey(Identifier)) {
            configAfterLoadListeners.put(Identifier, new java.util.ArrayList<>());
        }
        configAfterLoadListeners.get(Identifier).add(listener);
    }

    public static void registerConfigBeforeLoadListener(Pair<String, String> Identifier, Consumer<IConfig> listener) {
        if (!configBeforeLoadListeners.containsKey(Identifier)) {
            configBeforeLoadListeners.put(Identifier, new java.util.ArrayList<>());
        }
        configBeforeLoadListeners.get(Identifier).add(listener);
    }

    public static void registerConfigAfterSaveListener(Pair<String, String> Identifier, Consumer<IConfig> listener) {
        if (!configAfterSaveListeners.containsKey(Identifier)) {
            configAfterSaveListeners.put(Identifier, new java.util.ArrayList<>());
        }
        configAfterSaveListeners.get(Identifier).add(listener);
    }

    public static void registerConfigBeforeSaveListener(Pair<String, String> Identifier, Consumer<IConfig> listener) {
        if (!configBeforeSaveListeners.containsKey(Identifier)) {
            configBeforeSaveListeners.put(Identifier, new java.util.ArrayList<>());
        }
        configBeforeSaveListeners.get(Identifier).add(listener);
    }

    public static void executeConfigBeforeLoadListeners(Pair<String, String> Identifier, IConfig config) {
        if (configBeforeLoadListeners.containsKey(Identifier)) {
            for (Consumer<IConfig> listener : configBeforeLoadListeners.get(Identifier)) {
                listener.accept(config);
            }
        }
    }

    public static void executeConfigAfterLoadListeners(Pair<String, String> Identifier, IConfig config) {
        if (configAfterLoadListeners.containsKey(Identifier)) {
            for (Consumer<IConfig> listener : configAfterLoadListeners.get(Identifier)) {
                listener.accept(config);
            }
        }
    }

    public static void executeConfigBeforeSaveListeners(Pair<String, String> Identifier, IConfig config) {
        if (configBeforeSaveListeners.containsKey(Identifier)) {
            for (Consumer<IConfig> listener : configBeforeSaveListeners.get(Identifier)) {
                listener.accept(config);
            }
        }
    }

    public static void executeConfigAfterSaveListeners(Pair<String, String> Identifier, IConfig config) {
        if (configAfterSaveListeners.containsKey(Identifier)) {
            for (Consumer<IConfig> listener : configAfterSaveListeners.get(Identifier)) {
                listener.accept(config);
            }
        }
    }

    public static IConfig getConfig(Pair<String, String> Identifier) {
        return configRegistry.get(Identifier);
    }

    public static void loadAllConfigs() {
        for (Map.Entry<Pair<String, String>, IConfig> configEntry : configRegistry.entrySet()) {
            executeConfigBeforeLoadListeners(configEntry.getKey(), configEntry.getValue());
            configEntry.getValue().load();
            executeConfigAfterLoadListeners(configEntry.getKey(), configEntry.getValue());
        }
    }

    public static void saveAllConfigs() {
        for (Map.Entry<Pair<String, String>, IConfig> configEntry : configRegistry.entrySet()) {
            executeConfigBeforeSaveListeners(configEntry.getKey(), configEntry.getValue());
            configEntry.getValue().save();
            executeConfigAfterSaveListeners(configEntry.getKey(), configEntry.getValue());
        }
    }
}
