package io.github.josephdalughut.sidelock.android.utils.admin;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class DeviceAdministrator extends DeviceAdminReceiver{


    @Override
    public void onEnabled(Context context, Intent intent) {

    }


    @Override
    public void onDisabled(Context context, Intent intent) {
    }



}
