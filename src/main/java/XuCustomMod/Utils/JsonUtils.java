package XuCustomMod.Utils;

import basemod.Pair;
import com.google.gson.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

public class JsonUtils {
    private static class JsonType <T> {
        private final Function<JsonElement, Boolean> isTypeFunc;
        private final Function<JsonElement, T> jsonToObjectFunc;
        private final Function<T, JsonElement> objectToJsonFunc;
        public JsonType(Function<JsonElement, Boolean> isTypeFunc, Function<JsonElement, T> jsonToObjectFunc, Function<T, JsonElement> objectToJsonFunc) {
            this.isTypeFunc = isTypeFunc;
            this.jsonToObjectFunc = jsonToObjectFunc;
            this.objectToJsonFunc = objectToJsonFunc;
        }
        public Function<JsonElement, Boolean> getIsTypeFunc() {
            return isTypeFunc;
        }
        public Function<JsonElement, T> getJsonToObjectFunc() {
            return jsonToObjectFunc;
        }
        public Function<T, JsonElement> getObjectToJsonFunc() {
            return objectToJsonFunc;
        }
        public boolean isType(JsonElement jsonElement) {
            return isTypeFunc.apply(jsonElement);
        }
        public T jsonToObject(JsonElement jsonElement) {
            return jsonToObjectFunc.apply(jsonElement);
        }
        public JsonElement ObjectToJson(Object object) {
            return objectToJsonFunc.apply((T) object);
        }
    }

    private static final Method createJsonElementMethod;
    private static final JsonObject EMPTY_JSON_OBJECT = new JsonObject();

    static {
        try {
            createJsonElementMethod = JsonObject.class.getDeclaredMethod("createJsonElement", Object.class);
            createJsonElementMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static JsonElement createJsonElement(Object object){
        try {
            return (JsonElement) createJsonElementMethod.invoke(EMPTY_JSON_OBJECT, object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum JsonTypeEnum {
        STRING(String.class, new JsonType<String>(JsonElement::isJsonPrimitive, JsonElement::getAsString, JsonUtils::createJsonElement)),
        INT(Integer.class, new JsonType<Integer>(JsonElement::isJsonPrimitive, JsonElement::getAsInt, JsonUtils::createJsonElement)),
        LONG(Long.class, new JsonType<Long>(JsonElement::isJsonPrimitive, JsonElement::getAsLong, JsonUtils::createJsonElement)),
        BOOLEAN(Boolean.class, new JsonType<Boolean>(JsonElement::isJsonPrimitive, JsonElement::getAsBoolean, JsonUtils::createJsonElement)),
        FLOAT(Float.class, new JsonType<Float>(JsonElement::isJsonPrimitive, JsonElement::getAsFloat, JsonUtils::createJsonElement)),
        DOUBLE(Double.class, new JsonType<Double>(JsonElement::isJsonPrimitive, JsonElement::getAsDouble, JsonUtils::createJsonElement)),
        JSON_OBJECT(JsonObject.class, new JsonType<JsonObject>(JsonElement::isJsonObject, JsonElement::getAsJsonObject, (jsonObject -> jsonObject))),
        JSON_ARRAY(JsonArray.class, new JsonType<JsonArray>(JsonElement::isJsonObject, JsonElement::getAsJsonArray, (jsonArray -> jsonArray))),;

        <T> JsonTypeEnum(Class<T> clazz, JsonType<T> jsonType) {
            this.clazz = clazz;
            this.jsonType = jsonType;
        }

        private final Class<?> clazz;
        private final JsonType<?> jsonType;

        public JsonType<?> getJsonType() {
            return this.jsonType;
        }

        public static <T> JsonTypeEnum getJsonTypeEnum(T object) {
            for (JsonTypeEnum jsonTypeEnum : JsonTypeEnum.values()) {
                if (jsonTypeEnum.clazz.isInstance(object)) {
                    return jsonTypeEnum;
                }
            }
            throw new RuntimeException("No JsonTypeEnum found for " + object.getClass().getName());
        }

        public static <T> JsonType<T> getJsonType(T object) {
            JsonTypeEnum jsonTypeEnum = getJsonTypeEnum(object);
            if (jsonTypeEnum != null) {
                return (JsonType<T>) jsonTypeEnum.getJsonType();
            }
            return null;
        }
    }


    public static <T> T getOrDefault(JsonObject json, String Key, T defaultValue) {
        JsonType<T> jsonType = JsonTypeEnum.getJsonType(defaultValue);
        if (jsonType == null) {
            return defaultValue;
        }
        if (json.has(Key) && jsonType.isType(json.get(Key))) {
            return jsonType.jsonToObject(json.get(Key));
        }
        return defaultValue;
    }

    @SafeVarargs
    public static JsonObject createJsonObject(Pair<String, Object>... data) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Pair<String, Object> pair : data) {
            map.put(pair.getKey(), pair.getValue());
        }
        return createJsonObject(map);
    }

    public static JsonObject createJsonObject(Map<String, Object> data) {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                JsonObject jsonObject = createJsonObject((Map<String, Object>) value);
                json.add(entry.getKey(), jsonObject);
            } else if (value instanceof List) {
                JsonArray jsonArray = createJsonArray((List<Object>) value);
                json.add(entry.getKey(), jsonArray);
            } else {
                JsonType<?> jsonType = JsonTypeEnum.getJsonType(value);
                if (jsonType != null) {
                    json.add(entry.getKey(), jsonType.ObjectToJson(value));
                } else {
                    json.add(entry.getKey(), JsonNull.INSTANCE);
                }
            }
        }
        return json;
    }

    public static JsonArray createJsonArray(Object... data) {
        return createJsonArray(Arrays.asList(data));
    }

    public static JsonArray createJsonArray(List<Object> data) {
        JsonArray json = new JsonArray();
        for (Object value : data) {
            if (value instanceof Map) {
                JsonObject jsonObject = createJsonObject((Map<String, Object>) value);
                json.add(jsonObject);
            } else if (value instanceof List) {
                JsonArray jsonArray = createJsonArray((List<Object>) value);
                json.add(jsonArray);
            } else {
                JsonType<?> jsonType = JsonTypeEnum.getJsonType(value);
                if (jsonType != null) {
                    json.add(jsonType.ObjectToJson(value));
                } else {
                    json.add(JsonNull.INSTANCE);
                }
            }
        }
        return json;
    }
}
