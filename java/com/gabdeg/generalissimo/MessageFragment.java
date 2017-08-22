package com.gabdeg.generalissimo;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

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
import java.util.HashMap;
import java.util.Map;

public class MessageFragment extends Fragment {

    int gameID;
    Networker browser = new Networker();
    PageParser parser = new PageParser();

    private View view;

    ImageButton expandButton;

    ArrayList<NationChatPair> nations = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public boolean isFinishedLoading = false;
    boolean isExpanded = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        gameID = ((DiplomacyGame) getArguments().getSerializable(GameActivity.GAME_INFO))
                .getGameID();

        view = inflater.inflate(R.layout.message_fragment, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.message_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NationAdapter(nations,
                ((DiplomacyGame) getArguments().getSerializable(GameActivity.GAME_INFO)).getGameNation(),
                (AppCompatActivity) getActivity());
        mRecyclerView.setAdapter(mAdapter);


        expandButton = (ImageButton) view.findViewById(R.id.message_expand_button);
        expandButton.setBackground(null);
        expandButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isExpanded) {
                            retractFragment();
                        } else {
                            expandFragment();
                        }
                    }
                }
        );

        new GetNeighborsTask().execute(gameID);

        return view;
    }

    public void expandFragment() {
        TransitionManager.beginDelayedTransition(
                (CardView) view.findViewById(R.id.message_card_view)
        );
        mRecyclerView.setVisibility(View.VISIBLE);
        expandButton.setImageResource(R.drawable.unexpand);
        isExpanded = true;
    }

    public void retractFragment() {
        TransitionManager.beginDelayedTransition(
                (CardView) view.findViewById(R.id.message_card_view)
        );
        mRecyclerView.setVisibility(View.GONE);
        expandButton.setImageResource(R.drawable.expand);
        isExpanded = false;
    }

    public void refreshNeighbors() {
        isFinishedLoading = false;
        expandFragment();
        new GetNeighborsTask().execute(gameID);
    }

    private class GetNeighborsTask extends AsyncTask<Integer, Void, Integer> {

        protected Integer doInBackground(Integer... ints) {
            nations.clear();

            Elements webNations = new Elements();
            String gamePage = null;

            try {
                gamePage = browser.getAsString(
                        "http://webdiplomacy.net/board.php?gameID="
                        + String.valueOf(ints[0])
                );
                parser.loadFromStr(gamePage);
                Element webNationsTable = parser.select(".membersFullTable").first();
                webNations = webNationsTable.select(".member");

            } catch (Exception err) {
                err.printStackTrace();
                return 1;
            }


            String variantTitle = parser.select(".gamePanel").first().className().split(" ")[1]
                    .replace("variant", "");

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
                return 1;
            }

            CSSRuleList ruleList = styles.getCssRules();

            Nation global = new Nation();
            global.setName("Global");
            global.setId("country0");
            global.setUnits("Global");
            global.setCps("thingo is the best nondescriptor");
            global.setColor("#CE7800");

            Chat globalChat = null;
            try {
                globalChat = new Chat();
                globalChat.setUrl(
                        "http://webdiplomacy.net" +
                                parser.select("#chatboxtabs").first().select(".country0").first()
                                        .attr("href").substring(1)
                );
            } catch (Exception err) {
                globalChat = null;
            }

            nations.add(new NationChatPair(
                global, globalChat
            ));

            for (Element webNation : webNations) {

                //Log.v("MESSAGE_HTML", webNation.html());

                Nation nation = new Nation();
                Chat chat = new Chat();


                try {
                    nation.setId(
                            webNation.select("td").first().select(".memberCountryName").first()
                                    .select("span").get(2).className().split(" ")[0]
                    );
                } catch (IndexOutOfBoundsException err) {
                    try {
                        nation.setId(
                                webNation.select("td").first().select(".memberCountryName").first()
                                        .select("span").get(1).className().split(" ")[0]
                        );
                    } catch (Exception rr) {
                        rr.printStackTrace();
                        nation.setId("ERROR!");
                    }
                } catch (NullPointerException err) {
                    // pregame
                    nation.setId("none");
                    nation.setName("N/A");

                    nation.setUnits(parser.select(".occupationBarJoined").first().attr("style").split(":")[1] + " joined");
                    nation.setCps(parser.select(".occupationBarNotJoined").first().attr("style").split(":")[1] + " not joined");


                    nation.setColor("#000000");
                    nation.setOrderStatus("None");
                    chat = null;
                    nations.add(new NationChatPair(nation, chat));
                    continue;
                }

                nation.setName(webNation.select("td").first().select(".memberCountryName")
                        .first().select("." + nation.getId()).first().text());

                try {
                    nation.setCps(
                            webNation.select(".memberUnitCount").text().split(", ")[0]
                    );
                    if (nation.getCps().isEmpty()) {
                        nation.setCps("N/A");
                    }
                    nation.setUnits(
                            webNation.select(".memberUnitCount").text().split(", ")[1]
                    );
                    if (nation.getUnits().isEmpty()) {
                        nation.setUnits("N/A");
                    }
                } catch (Exception err) {
                    nation.setCps("N/A");
                    nation.setUnits("N/A");
                }

                for (int i = 0; i < ruleList.getLength(); i++) {
                    CSSRule rule = ruleList.item(i);
                    if (rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;
                        if (styleRule.getSelectorText().contains(nation.getId())) {
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


                                    float[] hsv = new float[3];

                                    Color.RGBToHSV(r, g, b, hsv);
                                    hsv[2] = 1;
                                    r = Color.red(Color.HSVToColor(hsv));
                                    g = Color.green(Color.HSVToColor(hsv));
                                    b = Color.blue(Color.HSVToColor(hsv));


                                    nation.setColor(
                                            String.format("#%02x%02x%02x", r, g, b));
                                    j = styleDeclaration.getLength();
                                    i = ruleList.getLength();
                                }
                            }
                        }
                    }
                }

                Element gameUserDetail = webNation.select("td").first().select(".memberCountryName").first();
                if (gameUserDetail.html().contains("Completed")) {
                    nation.setOrderStatus("Completed");
                } else if (gameUserDetail.html().contains("Ready")) {
                    nation.setOrderStatus("Ready");
                } else if (gameUserDetail.html().contains("Not received")) {
                    nation.setOrderStatus("Not received");
                } else if (gameUserDetail.html().contains("not completed")) {
                    nation.setOrderStatus("Not completed");
                } else {
                    nation.setOrderStatus("None");
                }


                try {
                    chat.setUrl(
                            "http://webdiplomacy.net" +
                                    parser.select("#chatboxtabs").first().select("." + nation.getId()).first()
                                            .attr("href").substring(1)
                    );
                    chat.setUnread(false);
                    if (parser.select("#chatboxtabs").first().select("." + nation.getId())
                            .first().html().contains("Unread message")) {
                        chat.setUnread(true);
                    }
                } catch (Exception err) {
                    chat = null;
                }

                nations.add(new NationChatPair(nation, chat));
            }

            return 0;
        }

        protected void onPostExecute(Integer status) {
            if (status != 0) {
            } else {
                mAdapter.notifyDataSetChanged();
            }

            isFinishedLoading = true;
            if (getActivity() != null) {
                ((GameActivity) getActivity()).isFinished();
            }

        }
    }

}
