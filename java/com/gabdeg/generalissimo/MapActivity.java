package com.gabdeg.generalissimo;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;


public class MapActivity extends AppCompatActivity {

    public boolean isFinishedLoading = false;

    int gameID = 0;
    DiplomacyGame game;
    int gameTurnOffset = 0;
    boolean gameGoToFirst = false;
    String mapUrl = null;

    boolean isPreviewing = false;

    SwipeRefreshLayout mSwipeRefreshLayout;

    Networker browser = new Networker();
    PageParser parser = new PageParser();

    private GetMapTask mapTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        game = ((DiplomacyGame) getIntent().getExtras().getSerializable(GameActivity.GAME_INFO));
        gameID = game.getGameID();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(game.getGameName());
        Log.v("GAME_ID", String.valueOf(gameID));
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.map_swipe_layout);
        mSwipeRefreshLayout.setDistanceToTriggerSync(480);

        mSwipeRefreshLayout.setEnabled(false);

        ((Button) findViewById(R.id.map_double_back)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gameGoToFirst = true;
                        refreshMap();
                    }
                }
        );
        ((Button) findViewById(R.id.map_back)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gameTurnOffset += -1;
                        refreshMap();
                    }
                }
        );
        ((Button) findViewById(R.id.map_preview)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isPreviewing = !isPreviewing;
                        refreshMap();
                    }
                }
        );
        ((Button) findViewById(R.id.map_front)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gameTurnOffset += 1;
                        refreshMap();
                    }
                }
        );
        ((Button) findViewById(R.id.map_double_front)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mapUrl = null;
                        refreshMap();
                    }
                }
        );

        refreshMap();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        getSupportActionBar().setTitle(game.getGameName());
    }

    public Drawable getDrawableFromURL(String url) {
        try {
            InputStream inp = browser.get(url);
            return Drawable.createFromStream(inp, "web");
        } catch (Exception err) {
            err.printStackTrace();
            return null;
        }
    }

    public void refreshMap() {
        mSwipeRefreshLayout.setRefreshing(true);
        isFinishedLoading = false;
        mapTask = new GetMapTask();
        mapTask.execute(gameID);
    }

    private class GetMapTask extends AsyncTask<Integer, Void, Drawable> {

        protected Drawable doInBackground(Integer... ints) {
            /*try {
                browser.post(
                        "http://webdiplomacy.net/index.php",
                        "loginuser=pieman2201&loginpass=REDACTED"
                );
            } catch (Exception err) {
                err.printStackTrace();
            }
            /*
            String url = "http://webdiplomacy.net/map.php?gameID=" +
                    String.valueOf(ints[1]) + "&turn=" +
                    String.valueOf(ints[0]) + "&mapType=large";
                    */
            if (mapUrl == null) {
                String gamePage = "Hello, world!";
                try {
                    gamePage = browser.getAsString("http://webdiplomacy.net/board.php?gameID=" +
                            String.valueOf(ints[0]));
                } catch (Exception err) {
                    err.printStackTrace();
                }
                parser.loadFromStr(gamePage);
                mapUrl = parser.select("#LargeMapLink").attr("href");
            }
            Log.v("MAP_URL", mapUrl);
            String[] mapQueries = mapUrl.split("&");
            int currentTurn = Integer.valueOf(mapQueries[1].split("=")[1]);
            mapQueries[1] = (gameGoToFirst ? "turn=-1" : mapQueries[1].split("=")[0] + "=" + String.valueOf(currentTurn + gameTurnOffset));
            Log.v("MAP_TURN", mapQueries[1]);
            mapUrl = StringUtils.join(mapQueries, "&");
            gameGoToFirst = false;
            gameTurnOffset = 0;
            Log.v("MAP_URL", mapUrl);

            Drawable mapImage = getDrawableFromURL("http://webdiplomacy.net/" + mapUrl  +
                    (isPreviewing ? "&preview" : ""));
            if (mapImage == null) {
                mapImage = ContextCompat.getDrawable(
                        getApplicationContext(), R.drawable.sadface);
            }


            return mapImage;
        }

        protected void onPostExecute(Drawable map) {
            try {
                ImageViewTouch image = (ImageViewTouch) findViewById(R.id.map);
                image.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
                image.setImageDrawable(map, null, 0.99f, 5);
            } catch (NullPointerException e) {
                Log.v("nullptr", "GetMapTask.onPostExecute");
            }
            isFinishedLoading = true;
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
