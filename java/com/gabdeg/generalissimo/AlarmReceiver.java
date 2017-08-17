package com.gabdeg.generalissimo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        Log.v("ALARM_RECEIVED", "I got an alarm! WAHOO!");

        Intent mServiceIntent = new Intent(context, CheckGameService.class);
        mServiceIntent.setData(Uri.parse("thingo"));
        context.startService(mServiceIntent);


    }

}
