package io.github.josephdalughut.sidelock.android.cache;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper for accessing default {@link android.content.SharedPreferences} for the app
 */
public class SharedPreferencesHelper {

    private static final String NAME = "LockPreferences";

    public static final String KEY_BOOT_ENABLED = "BOOT.enabled";
    public static final String KEY_SIDE_LOCK = "BUTTON.enabled";
    public static final String KEY_BUTTON_X = "BUTTON.x";
    public static final String KEY_BUTTON_Y = "BUTTON.y";

    /**
     * @param context
     * @return the default {@link SharedPreferences} for this app
     */
    public static SharedPreferences getInstance(Context context){
        return context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

}
