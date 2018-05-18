package io.github.josephdalughut.sidelock.android.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

import io.github.josephdalughut.sidelock.android.utils.admin.DeviceAdministrator;

/**
 * Handler class for accessing lock methods.
 */
public class LockManager {


    private DevicePolicyManager policyManager;
    private ComponentName comp;

    private Context context;

    /**
     * Initialize a new Lock Manager
     * @param context
     */
    public LockManager(Context context) {
        this.context = context;

        policyManager = (DevicePolicyManager)context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        comp = new ComponentName(context, DeviceAdministrator.class);

    }

    /**
     * @return true if the app has been granted the Device Administrator privileges
     */
    public boolean isDeviceAdmin(){
        return policyManager.isAdminActive(comp);
    }

    /**
     * Starts a request to grant the app Device Administrator privileges
     * @param fragment a {@link Fragment} to start the request, it's
     *                 {@link Fragment#onActivityResult(int, int, Intent)} method
     *                 would be called with the results
     */
    public void requestDeviceAdmin(Fragment fragment, int requestCode){
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, comp);

//        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "");

        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * Revokes the Device Administrator privilege
     */
    public void revokeDeviceAdmin(){
        policyManager.removeActiveAdmin(comp);
    }

    /**
     * Locks the device
     */
    public void lock(){
        policyManager.lockNow();
    }

}
