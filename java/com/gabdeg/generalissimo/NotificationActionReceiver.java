package com.gabdeg.generalissimo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("NOTIF_EVENT", String.valueOf(intent.getExtras().getInt("GAMEID")));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(
                String.valueOf(intent.getExtras().getInt("GAMEID")),
                String.valueOf(intent.getExtras().getString("GAMEPHASE"))
        );
        editor.apply();

    }

}
