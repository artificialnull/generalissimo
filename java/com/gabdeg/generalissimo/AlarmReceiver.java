package com.gabdeg.generalissimo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifStatus", true)) {
            Intent mServiceIntent = new Intent(context, CheckGameService.class);
            mServiceIntent.setData(Uri.parse("thingo"));
            context.startService(mServiceIntent);
        } else {
        }


    }

}
