package com.danielkim.soundrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Daniel on 5/22/2017.
 */

public class MySharedPreferences {
    private static String PREF_HIGH_QUALITY = "pref_high_quality";
    private static String PREF_HOST= "pref_host";
    private static String PREF_DEVICE = "pref_device";

    public static void setPrefHighQuality(Context context, boolean isEnabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_HIGH_QUALITY, isEnabled);
        editor.apply();
    }

    public static boolean getPrefHighQuality(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PREF_HIGH_QUALITY, false);
    }

    public static void setPrefHost(Context context, String host) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_HOST, host);
        editor.apply();
    }

    public static String getPrefHost(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_HOST, "127.0.0.1:5000");
    }

    public static void setPrefDevice(Context context, String device) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_DEVICE, device);
        editor.apply();
    }

    public static String getPrefDevice(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(PREF_DEVICE, "NEXUS 6");
    }

}
