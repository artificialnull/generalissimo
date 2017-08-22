package com.gabdeg.generalissimo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Map;


public class MainActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                Integer.parseInt(
                        settings.getString("notifCheckInterval", "1337")
                ),
                pendingIntent
        );

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        String password = settings.getString("dippyPasswd", null);
        if (password == null) {
            getUserCredentials(false);
        } else {
            onCredentialsGotten(
                    settings.getString("dippyUsernm", null),
                    settings.getString("dippyPasswd", null)
            );
        }

    }

    public void getUserCredentials(boolean wasInvalid) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("dippyPasswd");
        editor.remove("dippyUsernm");
        editor.apply();

        LoginDialogFragment loginDialogFragment = new LoginDialogFragment();
        if (wasInvalid) {
            loginDialogFragment.showInvalidBanner();
        }
        loginDialogFragment.show(getFragmentManager(), "login");


    }

    public void onCredentialsGotten(String username, String password) {
        // webDiplomacy caps usernames and passwords at 30 characters
        username = username.substring(0, Math.min(username.length(), 30));
        password = password.substring(0, Math.min(password.length(), 30));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("dippyUsernm", username);
        editor.putString("dippyPasswd", password);
        editor.apply();

        new ValidateCredentialsTask().execute(username, password);

    }

    private class ValidateCredentialsTask extends AsyncTask<String, Void, Integer> {

        protected Integer doInBackground(String... strings) {
            Networker browser = new Networker();
            String username = strings[0];
            String password = strings[1];

            try {

                String result = browser.postAsString(
                        "http://webdiplomacy.net/index.php",
                        "loginuser=" + username + "&loginpass=" + password
                );

                if (result.contains("<title>Error - webDiplomacy</title>")) {
                    return 1;
                }
            } catch (Exception err) {
                err.printStackTrace();
            }

            return 0;

        }

        protected void onPostExecute(Integer status) {

            if (status == 0) {
                onCredentialsValidated();
            } else {
                getUserCredentials(true);
            }
        }
    }

    public void onCredentialsValidated() {
        InputFragment firstFragment = new InputFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, firstFragment).commit();


        getSupportActionBar().setTitle("Home");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onRestart() {
        super.onRestart();
        getSupportActionBar().setTitle("Home");
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getString("dippyUsernm", "NotLogOut").equals("LogOut")) {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_credits) {
            Intent intent = new Intent(getApplicationContext(), CreditsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
