package com.gabdeg.generalissimo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;


public class CheckGameService extends IntentService {

    public CheckGameService() {
        super(null);
    }

    @Override
    public void onHandleIntent(Intent workIntent){
        String dataString = workIntent.getDataString();

        /*
        for (int i = 0; i < cookieManager.getCookieStore().getCookies().size(); i++) {
            Log.v("ALARM_COOKIE", "Already have: "
                    + cookieManager.getCookieStore().getCookies().get(i)
            );
        }

        if (CookieHandler.getDefault() != null) {
            for (int i = 0;
                 i < ((CookieManager) CookieHandler.getDefault())
                         .getCookieStore().getCookies().size();
                    i++) {
                cookieManager.getCookieStore().add(null,
                        ((CookieManager) CookieHandler.getDefault()).getCookieStore()
                                .getCookies().get(i)
                );
                Log.v("ALARM_COOKIE", "Added cookie to store: " + ((CookieManager) CookieHandler
                        .getDefault()).getCookieStore()
                        .getCookies().get(i));
            }

        } else {
            CookieHandler.setDefault(cookieManager);
        }

        Log.v("CookieStore", ((CookieManager) CookieHandler.getDefault()).getCookieStore()
                .getCookies().toString());
        */

        SharedPreferences settings = getSharedPreferences(MainActivity.PREFS_NAME, 0);
        String username = settings.getString("dippyUsernm", null);
        String password = settings.getString("dippyPasswd", null);
        if (password == null) {
            Log.v("ALARM_PASSWD", "Password is NULL!");
            return;
        }


        Elements games = new Elements();
        String homePage;
        Networker browser = new Networker();
        try {

            PageParser parser = new PageParser();

            String result = browser.postAsString(
                    "http://webdiplomacy.net/index.php",
                    "loginuser=" + username + "&loginpass=" + password
            );


            homePage = browser.getAsString("http://webdiplomacy.net/index.php");
            parser.loadFromStr(homePage);
            //Log.v("ALARM_HOMEPAGE", homePage);
            Element gamePanel = parser.select(".homeGamesStats").first();
            games = gamePanel.select(".gamePanelHome");
        } catch (Exception err) {
            err.printStackTrace();
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

            Log.v("ALARM_GAME_CHECKED", newGame.getGameName());

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

                                    j = styleDeclaration.getLength();
                                    i = ruleList.getLength();
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
                String gameStatus = "";
                Element gameUserDetail = game.select(".memberUserDetail").first();
                if (gameUserDetail.html().contains("Completed")) {
                    newGame.setGameOrderStatus("Completed");
                } else if (gameUserDetail.html().contains("Ready")) {
                    newGame.setGameOrderStatus("Ready");
                } else if (gameUserDetail.html().contains("Not received")) {
                    newGame.setGameOrderStatus("Not received");
                    gameStatus += "No orders received";
                } else if (gameUserDetail.html().contains("not completed")) {
                    newGame.setGameOrderStatus("Not completed");
                } else {
                    newGame.setGameOrderStatus("None");
                }

                if (gameUserDetail.html().contains("Unread message")) {
                    newGame.setGameMessageStatus(true);
                    if (!gameStatus.isEmpty()) {
                        gameStatus += " - ";
                    }
                    gameStatus += "Unread message";
                } else {
                    newGame.setGameMessageStatus(false);
                }

                if (!gameStatus.isEmpty()) {

                    Intent intent = new Intent(this, GameActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(GameActivity.GAME_INFO, newGame);
                    intent.putExtras(args);

                    PendingIntent pendingIntent = PendingIntent.getActivity(
                            this,
                            newGame.getGameID(),
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT);

                    Notification notif = new Notification.Builder(this)
                            .setContentTitle(newGame.getGameName())
                            .setContentText(gameStatus)
                            .setSmallIcon(R.drawable.army)
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent).build();

                    NotificationManager notifManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notifManager.notify(newGame.getGameID(), notif);
                }

            } catch (NullPointerException err) {
                newGame.setGameOrderStatus("None");
                newGame.setGameMessageStatus(false);
            }


        }

    }
}
