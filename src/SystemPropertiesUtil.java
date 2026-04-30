package com.example.hechuan.myfirstapplication.view_debug;

import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by songzhukai on 2019-12-20.
 */
public class SystemPropertiesUtil {
    private static final String TAG = "SystemPropertiesUtil";

    public static String getStr(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            Log.w(TAG, "getProperty catch exception, e=" + e);
        }

        return value;
    }

    public static long getLong(String key, long defaultValue) {
        long value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method getLong = c.getMethod("getLong", String.class, long.class);
            value = (long) getLong.invoke(c, key, defaultValue);
        } catch (Exception e) {
            Log.w(TAG, "getProperty catch exception, e=" + e);
        }

        return value;
    }
}
