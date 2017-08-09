package com.gabdeg.generalissimo;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.steadystate.css.parser.CSSOMParser;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;


import java.io.StringReader;
import java.util.ArrayList;


public class InputFragment extends Fragment {

    Networker browser = new Networker();
    PageParser parser = new PageParser();
    ArrayList<DiplomacyGame> diplomacyGames = new ArrayList<>();
    ArrayList<DiplomacyGame> backupDipGames = new ArrayList<>();
    SwipeRefreshLayout mSwipeRefreshLayout;

    boolean hasStartedIncrementing = false;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public void startRecyclerRefresh() {
        mSwipeRefreshLayout.setRefreshing(false);
        mAdapter = new GameAdapter(diplomacyGames, (AppCompatActivity) getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        final Handler h = new Handler();
        if (!hasStartedIncrementing) {
            h.postDelayed(new Runnable() {
                public void run() {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mAdapter.notifyItemRangeChanged(0, diplomacyGames.size(), 1);
                    }
                    h.postDelayed(this, 1000);
                }
            }, 1000);
            hasStartedIncrementing = true;
        }
    }

    private class GetGamesTask extends AsyncTask<Void, Void, Integer> {

        protected Integer doInBackground(Void... voids) {
            diplomacyGames.clear();

            /*try {
                browser.post(
                        "http://webdiplomacy.net/index.php",
                        "loginuser=pieman2201&loginpass=REDACTED"
                );
            } catch (Exception err) {
                err.printStackTrace();
            }*/

            Elements games = new Elements();
            String homePage;
            try {
                homePage = browser.getAsString("http://webdiplomacy.net/index.php");
                parser.loadFromStr(homePage);
                Element gamePanel = parser.select(".homeGamesStats").first();
                games = gamePanel.select(".gamePanelHome");
            } catch (Exception err) {
                err.printStackTrace();
            }

            if (games.size() <= 0) {
                return 1;
            }

            for (Element game : games) {

                String variantTitle = game.className().substring(21);
                Log.v("VARIANT_NAME", variantTitle);

                CSSStyleSheet styles = null;
                try {
                    styles = new CSSOMParser().parseStyleSheet(
                            new InputSource(
                                    new StringReader(
                                            browser.getAsString("http://webdiplomacy.net/variants/"
                                                    + variantTitle + "/resources/style.css")
                                    )
                            )
                    );
                } catch (Exception err) {
                    err.printStackTrace();
                }
                CSSRuleList ruleList = styles.getCssRules();

                DiplomacyGame newGame = new DiplomacyGame();

                Element gameTitleBar = game.select(".homeGameTitleBar").first();
                newGame.setGameID(Integer.parseInt(gameTitleBar.attr("gameid")));
                newGame.setGameName(gameTitleBar.text());

                Element gameTimeRemaining = game.select(".timeremaining").first();
                newGame.setGameTimeOfPhase(Long.parseLong(gameTimeRemaining.attr("unixtime")));
                Log.v(newGame.getGameName(), String.valueOf(newGame.getGameTimeOfPhase()));

                Element gamePhaseLine = game.select(".titleBarRightSide").get(1);
                newGame.setGamePhaseDate(
                        gamePhaseLine.select(".gameDate").first().text()
                );
                newGame.setGamePhaseType(
                        gamePhaseLine.select(".gamePhase").first().text()
                );

                Nation nation = new Nation();

                try {
                    nation.setName(game.select(".memberCountryName").first().text());
                    Element gameCountrySpan = game.select(".memberYourCountry").first();
                    String countrySelector = "." + gameCountrySpan.className().split(" ")[0];
                    for (int i = 0; i < ruleList.getLength(); i++) {
                        CSSRule rule = ruleList.item(i);
                        if (rule instanceof CSSStyleRule) {
                            CSSStyleRule styleRule = (CSSStyleRule) rule;
                            if (styleRule.getSelectorText().contains(countrySelector)) {
                                CSSStyleDeclaration styleDeclaration = styleRule.getStyle();

                                for (int j = 0; j < styleDeclaration.getLength(); j++) {
                                    String property = styleDeclaration.item(j);
                                    if (property.equals("color")) {
                                        String colorString = styleDeclaration
                                                .getPropertyCSSValue(property).getCssText();
                                        colorString = colorString.substring(4, colorString.length()-1);
                                        String[] colors = colorString.split(", ");
                                        int r = Integer.parseInt(colors[0]);
                                        int g = Integer.parseInt(colors[1]);
                                        int b = Integer.parseInt(colors[2]);

                                        Log.v("OldNationColor", String.format("#%02x%02x%02x", r, g, b));

                                        float[] hsv = new float[3];

                                        Color.RGBToHSV(r, g, b, hsv);
                                        hsv[2] = 1;
                                        r = Color.red(Color.HSVToColor(hsv));
                                        g = Color.green(Color.HSVToColor(hsv));
                                        b = Color.blue(Color.HSVToColor(hsv));


                                        nation.setColor(
                                                String.format("#%02x%02x%02x", r, g, b));
                                    }
                                }
                            }
                        }
                    }
                } catch (NullPointerException err) {
                    nation.setName("N/A");
                    nation.setColor("#000000");
                }


                nation.setUnits("N/A");
                nation.setCps("N/A");
                //Element gameUnitLine = game.select(".memberSCCount").first();
                //nation.setCps(gameUnitLine.text().split(", ")[0]);
                //nation.setUnits(gameUnitLine.text().split(", ")[1]);

                newGame.setGameNation(nation);
                Log.v("NewNationColor", newGame.getGameNation().getColor());

                try {
                    Element gameUserDetail = game.select(".memberUserDetail").first();
                    if (gameUserDetail.html().contains("Completed")) {
                        newGame.setGameOrderStatus("Completed");
                    } else if (gameUserDetail.html().contains("Ready")) {
                        newGame.setGameOrderStatus("Ready");
                    } else if (gameUserDetail.html().contains("Not received")) {
                        newGame.setGameOrderStatus("Not received");
                    } else if (gameUserDetail.html().contains("not completed")) {
                        newGame.setGameOrderStatus("Not completed");
                    } else {
                        newGame.setGameOrderStatus("None");
                    }

                    if (gameUserDetail.html().contains("Unread message")) {
                        newGame.setGameMessageStatus(true);
                    } else {
                        newGame.setGameMessageStatus(false);
                    }
                } catch (NullPointerException err) {
                    newGame.setGameOrderStatus("None");
                    newGame.setGameMessageStatus(false);
                }


                diplomacyGames.add(newGame);
            }
            return 0;
        }

        protected void onPostExecute(Integer status) {
            if (status != 0) {
                Toast.makeText(getContext(),
                        "Could not load webDiplomacy games!", Toast.LENGTH_LONG)
                        .show();
                mSwipeRefreshLayout.setRefreshing(false);
            } else {
                for (DiplomacyGame diplomacyGame : diplomacyGames) {
                    Log.v(diplomacyGame.getGameIDStr(), diplomacyGame.getGameName());
                }
                startRecyclerRefresh();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.input_fragment, container, false);


        /*
        Button submitButton = (Button) view.findViewById(R.id.button);
        submitButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText textBox = (EditText) getActivity().findViewById(R.id.textBox);
                        int gameID = Integer.parseInt(textBox.getText().toString());
                        Log.v("gameID", String.valueOf(gameID));
                        mCallback.onGameIDSubmitted(gameID);

                    }
                }
        );
        */
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.input_fragment);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                backupDipGames.clear();
                for (DiplomacyGame game : diplomacyGames) {
                    backupDipGames.add(game);
                }
                mAdapter = new GameAdapter(backupDipGames, (AppCompatActivity) getActivity());
                mRecyclerView.setAdapter(mAdapter);
                new GetGamesTask().execute();
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.game_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (diplomacyGames.isEmpty()) {
            mSwipeRefreshLayout.setRefreshing(true);
            new GetGamesTask().execute();
        } else {
            startRecyclerRefresh();
        }

        return view;
    }

}
