package de.hdm_stuttgart.jammin.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * This is the apps main toast handler.
 *
 * It notifies the user on every activity where it is registered
 * about everything that happens in the threads making the connections.
 */

public class ToastHandler {

    final public static int USER_NOTIFICATION_TOAST = 100;

    private static Handler toastHandler;

    public static Handler getToastHandler(final Context context) {
        if(toastHandler == null){
            toastHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case USER_NOTIFICATION_TOAST:
                            String notification = (String) msg.obj;
                            Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        return toastHandler;
    }
}

