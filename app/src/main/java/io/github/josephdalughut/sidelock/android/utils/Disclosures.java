package io.github.josephdalughut.sidelock.android.utils;

/**
 * Created by Joseph Dalughut on 08/12/2018
 * Copyright © 2018
 *
 * Disclosures shown within the app
 */
public class Disclosures {

    public static String DEVICE_ADMIN = "To enable this feature, you need to grant this app the Device Administrator permission (**BIND_DEVICE_ADMIN**) which allows this app lock your phone screen. The following security polices will be used:\n" +
            "- Lock device immediately (*force-lock*): Allows this app to lock your phone without you pressing the power button.\n" +
            "\n" +
            "You will be taken to a seperate screen to grant this permission which can be revoked anytime in your settings. To cancel this request, click **Back**.";


    public static String OVERLAY = "To enable this feature, you need to grant this app the permission to draw a lock button on your screen and over other apps (**SYSTEM_ALERT_WINDOW**).\n" +
            "\n" +
            "You will be taken to a seperate screen to grant this permission which can be revoked anytime under this apps' settings. To cancel this request, click **Back**.";

}
