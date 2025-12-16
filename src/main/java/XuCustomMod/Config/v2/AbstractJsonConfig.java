package XuCustomMod.Config.v2;

import XuCustomMod.Config.IConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import static basemod.BaseMod.gson;

public abstract class AbstractJsonConfig implements IConfig {
    private final String NameSpace;
    private final String Path;
    private static final String EXTENSION = "json";

    public static final Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

    public JsonObject createDefault() {
        return this.saveToJson();
    }

    public static Boolean IsJsonComplete(JsonObject defaultJsonObject, JsonObject loadingJsonObject) {
        for (Map.Entry<String, JsonElement> entry : defaultJsonObject.entrySet()) {
            if (!loadingJsonObject.has(entry.getKey())) {
                return false;
            }
            if (entry.getValue().isJsonObject() && loadingJsonObject.get(entry.getKey()).isJsonObject()) {
                if (!IsJsonComplete(entry.getValue().getAsJsonObject(), loadingJsonObject.get(entry.getKey()).getAsJsonObject())) {
                    return false;
                }
            }
        }
        return true;
    }

    public Boolean IsJsonComplete(JsonObject jsonObject) {
        return IsJsonComplete(this.createDefault(), jsonObject);
    }

    public abstract void loadFormJson(JsonObject jsonObject);

    public abstract JsonObject saveToJson();

    public String getConfigFilePath() {
        return SpireConfig.makeFilePath(this.NameSpace, this.Path, EXTENSION);
    }

    public AbstractJsonConfig(String NameSpace, String Path) {
        this.NameSpace = NameSpace;
        this.Path = Path;
    }

    public static JsonObject ReadJsonFile(File jsonFile){
        try {
            String jsonString = Files.readAllLines(jsonFile.toPath(), StandardCharsets.UTF_8).stream().reduce("", String::concat);
            return gson.fromJson(jsonString, JsonObject.class);
        } catch (IOException e) {
            return null;
        }
    }

    private static String jsonObjectToString(JsonObject jsonObject, Boolean minify) {
        if (minify) {
            return gson.toJson(jsonObject);
        } else {
            return gsonPretty.toJson(jsonObject);
        }
    }

    public static void WriteJsonFile(File jsonFile, JsonObject jsonObject, Boolean minify) {
        try {
            Files.write(jsonFile.toPath(), jsonObjectToString(jsonObject, minify).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void load() {
        try {
            File configFile = new File(this.getConfigFilePath());
            boolean NeedUpdate = false;
            JsonObject jsonObject;
            if (!configFile.exists() || configFile.length() == 0) {
                configFile.createNewFile();
                jsonObject = this.createDefault();
                this.save(jsonObject);
            } else {
                jsonObject = ReadJsonFile(configFile);
                NeedUpdate = !this.IsJsonComplete(jsonObject);
            }
            this.loadFormJson(jsonObject);
            if (NeedUpdate) {
                this.save();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save() {
        this.save(this.saveToJson());
    }

    public void save(JsonObject jsonObject) {
        WriteJsonFile(new File(this.getConfigFilePath()), jsonObject, false);
    }
}
