package com.gabdeg.generalissimo;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


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

        Log.v("GAME_ID", String.valueOf(game.getGameID()));

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

        Log.v("GAME_FRAGMENT", "REFRESHING!");
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
        Log.v("IS_FINISHED", "Someone finished doing something I guess...");
        if (orderFragment.isFinishedLoading && infoFragment.isFinishedLoading
                && messageFragment.isFinishedLoading) {
            mSwipeRefreshLayout.setRefreshing(false);
            Log.v("EVERYONE_IS_FINISHED", "Wahoo!");
        }
    }

}
