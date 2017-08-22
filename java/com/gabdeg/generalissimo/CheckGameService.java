package com.gabdeg.generalissimo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.TaskStackBuilder;
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


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String username = settings.getString("dippyUsernm", null);
        String password = settings.getString("dippyPasswd", null);
        if (password == null) {
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
            Element gamePanel = parser.select(".homeGamesStats").first();
            games = gamePanel.select(".gamePanelHome");
        } catch (Exception err) {
            err.printStackTrace();
        }

        for (Element game : games) {

            String variantTitle = game.className().substring(21);

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


            try {
                String gameStatus = "";
                Element gameUserDetail = game.select(".memberUserDetail").first();
                if (gameUserDetail.html().contains("Completed")) {
                    newGame.setGameOrderStatus("Completed");
                } else if (gameUserDetail.html().contains("Ready")) {
                    newGame.setGameOrderStatus("Ready");
                } else if (gameUserDetail.html().contains("Not received")) {
                    newGame.setGameOrderStatus("Not received");
                    if (settings.getBoolean("notifNMRStatus", true)) {
                        gameStatus += "No orders received";
                    } else {
                    }
                } else if (gameUserDetail.html().contains("not completed")) {
                    newGame.setGameOrderStatus("Not completed");
                } else {
                    newGame.setGameOrderStatus("None");
                }

                if (gameUserDetail.html().contains("Unread message")) {
                    newGame.setGameMessageStatus(true);
                    if (settings.getBoolean("notifUnreadStatus", true)) {
                        if (!gameStatus.isEmpty()) {
                            gameStatus += " - ";
                        }
                        gameStatus += "Unread message";
                    } else {
                    }
                } else {
                    newGame.setGameMessageStatus(false);
                }

                if (!gameStatus.isEmpty() &&
                        !settings.getString(String.valueOf(newGame.getGameID()), "CrashOverride")
                                .equals(newGame.getGamePhaseDate() + newGame.getGamePhaseType())) {

                    Intent intent = new Intent(this, GameActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(GameActivity.GAME_INFO, newGame);
                    intent.putExtras(args);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(GameActivity.class);
                    stackBuilder.addNextIntent(intent);


                    PendingIntent pendingIntent = stackBuilder.getPendingIntent(
                            newGame.getGameID(),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    Notification notif = new Notification.Builder(this)
                            .setContentTitle(newGame.getGameName())
                            .setContentText(gameStatus)
                            .setSmallIcon(R.drawable.army)
                            .addAction(
                                    new Notification.Action.Builder(
                                            R.drawable.remove,
                                            "DISMISS FOR PHASE",
                                            PendingIntent.getBroadcast(
                                                    this, 1,
                                                    new Intent(this, NotificationActionReceiver.class)
                                                            .putExtra("GAMEID", newGame.getGameID())
                                                            .putExtra("GAMEPHASE", newGame.getGamePhaseDate() + newGame.getGamePhaseType()),
                                                    PendingIntent.FLAG_CANCEL_CURRENT
                                            )
                                    ).build()
                            )
                            /*
                            .addAction(
                                    new Notification.Action.Builder(
                                            R.drawable.army,
                                            "DISABLE FOR GAME",
                                            PendingIntent.getBroadcast(
                                                    this, 2,
                                                    new Intent(this, NotificationActionReceiver.class),
                                                    PendingIntent.FLAG_CANCEL_CURRENT
                                            )
                                    ).build()
                            )*/
                            .setAutoCancel(true)
                            .setContentIntent(pendingIntent).build();

                    NotificationManager notifManager =
                            (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    notifManager.notify(newGame.getGameID(), notif);
                } else if (settings.getString(String.valueOf(newGame.getGameID()), "CrashOverride")
                        .equals(newGame.getGamePhaseDate() + newGame.getGamePhaseType())) {
                }

            } catch (NullPointerException err) {
                newGame.setGameOrderStatus("None");
                newGame.setGameMessageStatus(false);
            }


        }

    }
}
