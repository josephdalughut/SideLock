package io.github.josephdalughut.sidelock.android.broadcast;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import io.github.josephdalughut.sidelock.android.cache.SharedPreferencesHelper;
import io.github.josephdalughut.sidelock.android.service.LockService;

public class OnBootReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = OnBootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "Boot completed, starting service");
        LockService.start(context, false);
    }

    /**
     * Helper method to enable or disable this receiver programmatically
     * @param context
     * @param enabled set to true to enable, false otherwise
     */
    public static void setEnabled(Context context, boolean enabled){
        ComponentName component = new ComponentName(context, OnBootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                component,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        //put this in shared preferences so we know state
        SharedPreferencesHelper.getInstance(context).edit().putBoolean(SharedPreferencesHelper.KEY_BOOT_ENABLED, enabled).apply();
    }

}
