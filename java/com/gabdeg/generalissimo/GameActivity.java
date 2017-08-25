package com.gabdeg.generalissimo;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class GameActivity extends AppCompatActivity {

    final static String GAME_INFO = "GAME_INFO";

    DiplomacyGame game;
    SwipeRefreshLayout mSwipeRefreshLayout;

    InfoFragment infoFragment;
    OrderFragment orderFragment;
    MessageFragment messageFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        game = (DiplomacyGame) getIntent().getExtras().getSerializable(GAME_INFO);

        setContentView(R.layout.activity_game);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(game.getGameName());

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        new ValidateCredentialsTask().execute(
                settings.getString("dippyUsernm", null),
                settings.getString("dippyPasswd", null)
        );
    }

    private class ValidateCredentialsTask extends AsyncTask<String, Void, Integer> {

        protected Integer doInBackground(String... strings) {
            Networker browser = new Networker();
            String username = strings[0];
            String password = strings[1];
            try {
                browser.postAsString(
                        "http://webdiplomacy.net/index.php",
                        "loginuser=" + username + "&loginpass=" + password
                );
            } catch (Exception err) {
                err.printStackTrace();
                return 1;
            }
            return 0;

        }

        protected void onPostExecute(Integer status) {
            if (status == 0) {
                onLoggedIn();
            }
        }
    }

    public void onLoggedIn() {
        infoFragment = new InfoFragment();
        orderFragment = new OrderFragment();
        messageFragment = new MessageFragment();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.map_fragment);
        mSwipeRefreshLayout.setDistanceToTriggerSync(480);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshGame();
            }
        });
        mSwipeRefreshLayout.setRefreshing(true);

        Bundle args = new Bundle();
        args.putSerializable(GAME_INFO, game);
        infoFragment.setArguments(args);
        orderFragment.setArguments(args);
        messageFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.map_container, infoFragment);
        transaction.commit();

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.message_container, messageFragment);
        transaction.commit();

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.order_container, orderFragment);
        transaction.commit();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        getSupportActionBar().setTitle(game.getGameName());
    }

    public void refreshGame() {
        mSwipeRefreshLayout.setRefreshing(true);

        infoFragment.refreshInfo();
        messageFragment.refreshNeighbors();
        orderFragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putSerializable(GAME_INFO, game);
        orderFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.order_container, orderFragment);
        transaction.commit();

    }

    public void isFinished() {
        if (orderFragment.isFinishedLoading && infoFragment.isFinishedLoading
                && messageFragment.isFinishedLoading) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

}
