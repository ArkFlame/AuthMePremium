package com.arkflame.authmepremium.utils;

import java.lang.reflect.Field;

/*
 * Utilities to make reflection easier.
 */
public class HandlerReflectionUtil {
    public static <T> T getFieldValue(Object object, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static void setFieldValue(Object object, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
