package com.gabdeg.generalissimo;


import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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

    ArrayList<NationChatPair> nations = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        gameID = ((DiplomacyGame) getArguments().getSerializable(GameActivity.GAME_INFO))
                .getGameID();

        View view = inflater.inflate(R.layout.message_fragment, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.message_recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new NationAdapter(nations, (AppCompatActivity) getActivity());
        mRecyclerView.setAdapter(mAdapter);

        new GetNeighborsTask().execute(gameID);

        return view;

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
                return 1;
            }

            CSSRuleList ruleList = styles.getCssRules();

            for (Element webNation : webNations) {

                //Log.v("MESSAGE_HTML", webNation.html());

                Nation nation = new Nation();

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

                Log.v("MESSAGE_NATION", nation.toString());

                nations.add(new NationChatPair(nation, null));
            }

            return 0;
        }

        protected void onPostExecute(Integer status) {
            if (status != 0) {
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

}
