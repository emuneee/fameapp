package co.mainmethod.fame.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Allows the easy setting of user preferences
 * Created by evan on 1/31/16.
 */
public class PrefUtil {

    private static final String APP_PREFS_NAME = "FamePrefs";
    private static final String PREF_USE_FF_CAM = "UseFrontFacingCamera";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_APPEND);
    }

    public static boolean useFrontFacingCamera(Context context) {
        return getPreferences(context).getBoolean(PREF_USE_FF_CAM, false);
    }

    public static void setUseFrontFacingCamera(Context context, boolean value) {
        getPreferences(context)
                .edit()
                .putBoolean(PREF_USE_FF_CAM, value)
                .commit();
    }

}
