package XuCustomMod.Utils;

import XuCustomMod.XuCustomMod;
import basemod.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class ReflectUtils {
    public static final boolean DEBUG_MODE = false;

    // XuCustomMod/Utils/ReflectUtils, true -> ReflectUtils[].class
    public static Class<?> getClass(String Name, boolean IsArray) {
        String replacedName = Name.replace("/", ".");
        try {
            if (IsArray) {
                return Class.forName(replacedName + "[]");
            } else {
                return Class.forName(replacedName);
            }
        } catch (ClassNotFoundException e) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getClass] Class not found: " + Name);
            if (DEBUG_MODE) {
                throw new RuntimeException(e);
            }
            return null;
        }
    };

    // Z：boolean
    // B：byte
    // C：char
    // S：short
    // I：int
    // J：long
    // F：float
    // D：double
    // V：void

    // ZZ[Ljava/lang/String; -> {boolean.class, boolean.class, String[].class}
    public static Class<?>[] parseParamTypes(String ParamTypes) {
        if (ParamTypes.isEmpty()) {
            return new Class[0];
        }
        int nowPtr = 0;
        int length = ParamTypes.length();
        int objectBegin = 0;
        boolean isReadingObject = false;
        boolean isReadingArray = false;
        List<Class<?>> ParamTypeList = new ArrayList<>();
        while (nowPtr < length) {
            char nowChar = ParamTypes.charAt(nowPtr);
            if (isReadingObject) {
                if (nowChar == ';') {
                    String className = ParamTypes.substring(objectBegin, nowPtr);
                    Class<?> Class = getClass(className, isReadingArray);
                    if (Class == null) {
                        XuCustomMod.LOGGER.error("[ReflectUtils.parseParamTypes] Class not found: " + className);
                        return null;
                    }
                    ParamTypeList.add(Class);
                    isReadingObject = false;
                    isReadingArray = false;
                }
            } else if (nowChar == '[') {
                isReadingArray = true;
            } else {
                switch (nowChar) {
                    case 'Z':
                        ParamTypeList.add(boolean.class);
                        break;
                    case 'B':
                        ParamTypeList.add(byte.class);
                        break;
                    case 'C':
                        ParamTypeList.add(char.class);
                        break;
                    case 'S':
                        ParamTypeList.add(short.class);
                        break;
                    case 'I':
                        ParamTypeList.add(int.class);
                        break;
                    case 'J':
                        ParamTypeList.add(long.class);
                        break;
                    case 'F':
                        ParamTypeList.add(float.class);
                        break;
                    case 'D':
                        ParamTypeList.add(double.class);
                        break;
                    case 'V':
                        ParamTypeList.add(void.class);
                        break;
                    case 'L':
                        isReadingObject = true;
                        objectBegin = nowPtr + 1;
                        break;
                    default:
                        XuCustomMod.LOGGER.error("[ReflectUtils.parseParamTypes] Invalid character: {}", nowChar);
                        return null;
                }
            }
            nowPtr++;
        }
        return ParamTypeList.toArray(new Class<?>[0]);
    }

    // abc(Z[Ljava/lang/String;)Ljava/lang/String; -> Pair<"abc", Pair<String.class, {boolean.class, String[].class}>>
    public static Pair<String, Pair<Class<?>, Class<?>[]>> parseMethodDescription(String MethodDescription) {
        String MethodName = MethodDescription.substring(0, MethodDescription.indexOf("("));
        String MethodParams = MethodDescription.substring(MethodDescription.indexOf("(") + 1, MethodDescription.indexOf(")"));
        String MethodReturnType = MethodDescription.substring(MethodDescription.indexOf(")") + 1);
        Class<?>[] MethodParamTypes = parseParamTypes(MethodParams);
        Class<?>[] MethodReturnTypes = parseParamTypes(MethodReturnType);
        if (MethodParamTypes == null || MethodReturnTypes == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.parseMethodDescription] Failed to parse method description: {}", MethodDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to parse method description: " + MethodDescription);
            }
            return null;
        }
        if (MethodReturnTypes.length != 1) {
            XuCustomMod.LOGGER.error("[ReflectUtils.parseMethodDescription] Invalid return type length: {} method description: {}", MethodReturnTypes.length, MethodDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Invalid return type length: " + MethodReturnTypes.length + " method description: " + MethodDescription);
            }
            return null;
        }
        return new Pair<>(MethodName, new Pair<>(MethodReturnTypes[0], MethodParamTypes));
    }

    // c.class, abc(Z[Ljava/lang/String;)Ljava/lang/String; -> String abc(boolean param1, String[] param2)
    public static Method getMethod(Class<?> methodClass, String MethodDescription) {
        Pair<String, Pair<Class<?>, Class<?>[]>> MethodInfo = parseMethodDescription(MethodDescription);
        if (MethodInfo == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getMethod] Failed to parse method description: {}", MethodDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to parse method description: " + MethodDescription);
            }
            return null;
        }
        String MethodName = MethodInfo.getKey();
        Class<?>[] MethodParamTypes = MethodInfo.getValue().getValue();
        Class<?> MethodReturnType = MethodInfo.getValue().getKey();
        try {
            Method method = methodClass.getDeclaredMethod(MethodName, MethodParamTypes);
            if (method.getReturnType() != MethodReturnType) {
                XuCustomMod.LOGGER.error("[ReflectUtils.getMethod] Invalid return type: {} method description: {}", method.getReturnType(), MethodDescription);
                if (DEBUG_MODE) {
                    throw new RuntimeException("Invalid return type: " + method.getReturnType() + " method description: " + MethodDescription);
                }
                return null;
            }
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getMethod] Failed to get method: {}", MethodDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    // La.b.c;abc(Z[Ljava/lang/String;)Ljava/lang/String; -> String abc(boolean param1, String[] param2)
    public static Method getMethod(String fullMethodDescription) {
        assert fullMethodDescription.charAt(0) == 'L';
        String methodClass = fullMethodDescription.substring(1, fullMethodDescription.indexOf(";"));
        String MethodDescription = fullMethodDescription.substring(fullMethodDescription.indexOf(";") + 1);
        try {
            Class<?> methodClassObj = Class.forName(methodClass.replace("/", "."));
            return getMethod(methodClassObj, MethodDescription);
        } catch (ClassNotFoundException e) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getMethod] Failed to get method: {}", fullMethodDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    // La.b.c([Z) -> c(boolean[])
    public static Constructor<?> getConstructor(String fullConstructorDescription) {
        assert fullConstructorDescription.charAt(0) == 'L';
        String targetClass = fullConstructorDescription.substring(1, fullConstructorDescription.indexOf("("));
        String description = fullConstructorDescription.substring(fullConstructorDescription.indexOf("(;)") + 1, fullConstructorDescription.indexOf(")"));
        Class<?> targetClassObj = getClass(targetClass, false);
        Class<?>[] paramTypes = parseParamTypes(description);
        if (targetClassObj == null || paramTypes == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getConstructor] Failed to get constructor: {}", fullConstructorDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get constructor: " + fullConstructorDescription);
            }
            return null;
        }
        Constructor<?>[] constructors = targetClassObj.getDeclaredConstructors();
        Constructor<?> finalConstructor = null;
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            if (parameters.length == paramTypes.length) {
                boolean match = true;
                for (int parametersIndex = 0; parametersIndex < parameters.length; parametersIndex++) {
                    Class<?> nowParametersType = parameters[parametersIndex].getType();
                    Class<?> nowConstructorType = paramTypes[parametersIndex];
                    if (nowParametersType != nowConstructorType) {
                        XuCustomMod.LOGGER.warn("Parameter type [ " + nowParametersType.getName() + " ] does not match arg type [ " + nowConstructorType.getName() + " ]");
                        match = false;
                        break;
                    }
                }
                if (match) {
                    finalConstructor = constructor;
                    break;
                }
            }
        }
        if (finalConstructor == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getConstructor] Failed to get constructor: {}", fullConstructorDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get constructor: " + fullConstructorDescription);
            }
            return null;
        }
        return finalConstructor;
    }

    // La.b.c;abc -> Field abc
    public static Field getField(String fullFieldDescription) {
        assert fullFieldDescription.charAt(0) == 'L';
        String targetClass = fullFieldDescription.substring(1, fullFieldDescription.indexOf(";"));
        String fieldName = fullFieldDescription.substring(fullFieldDescription.indexOf(";") + 1);
        Class<?> targetClassObj = getClass(targetClass, false);
        if (targetClassObj == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getField] Failed to get field: {}", fullFieldDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get field: " + fullFieldDescription);
            }
            return null;
        }
        try {
            return targetClassObj.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getField] Failed to get field: {}", fullFieldDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }

    // La.b.c;abc()I -> int abc
    public static <T> T getFieldValue(String fullFieldDescription) {
        String FieldDescription = fullFieldDescription.substring(fullFieldDescription.indexOf("(") + 1);
        String ValueTypeDescription = fullFieldDescription.substring(fullFieldDescription.indexOf(")") + 1);
        Field field = getField(fullFieldDescription);
        if (field == null) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getFieldValue] Failed to get field: {}", fullFieldDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get field: " + fullFieldDescription);
            }
            return null;
        }
        Class<?>[] ValueType = parseParamTypes(ValueTypeDescription);
        if (ValueType.length != 1) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getFieldValue] Failed to get field, ValueType length must Be 1 {} : {}", ValueType.length, fullFieldDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get field: " + fullFieldDescription);
            }
            return null;
        }
        Class<?> ValueTypeClass = ValueType[0];
        Class<?> fieldType = field.getType();
        if (fieldType != ValueTypeClass) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getFieldValue] Failed to get field, fieldType [ " + fieldType.getName() + " ] does not match ValueType [ " + ValueTypeClass.getName() + " ]");
            if (DEBUG_MODE) {
                throw new RuntimeException("Failed to get field: " + fullFieldDescription);
            }
            return null;
        }
        try {
            return (T) field.get(null);
        } catch (Exception e) {
            XuCustomMod.LOGGER.error("[ReflectUtils.getFieldValue] Failed to get field: {}", fullFieldDescription);
            if (DEBUG_MODE) {
                throw new RuntimeException(e);
            }
            return null;
        }
    }
}
