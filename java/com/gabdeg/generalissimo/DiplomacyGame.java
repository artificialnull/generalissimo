package com.gabdeg.generalissimo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DiplomacyGame implements Serializable {
    private String  gameName;
    private int     gameID;
    private long    gameTimeOfPhase;
    private String  gamePhaseDate;
    private String  gamePhaseType;
    private Nation  gameNation;
    private String  gameOrderStatus;
    private boolean gameMessageStatus;

    public Nation getGameNation() {
        return gameNation;
    }

    public void setGameNation(Nation gameNation) {
        this.gameNation = gameNation;
    }

    public String getGameOrderStatus() {
        return gameOrderStatus;
    }

    public void setGameOrderStatus(String gameOrderStatus) {
        this.gameOrderStatus = gameOrderStatus;
    }

    public boolean getGameMessageStatus() {
        return gameMessageStatus;
    }

    public void setGameMessageStatus(boolean gameMessageStatus) {
        this.gameMessageStatus = gameMessageStatus;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameIDStr() {
        return String.valueOf(getGameID());
    }

    public long getGameTimeOfPhase() {
        return gameTimeOfPhase;
    }

    public void setGameTimeOfPhase(long gameTimeOfPhase) {
        this.gameTimeOfPhase = gameTimeOfPhase;
    }

    public String getGamePhaseDate() {
        return gamePhaseDate;
    }

    public void setGamePhaseDate(String gamePhaseDate) {
        this.gamePhaseDate = gamePhaseDate;
    }

    public String getGamePhaseType() {
        return gamePhaseType;
    }

    public void setGamePhaseType(String gamePhaseType) {
        this.gamePhaseType = gamePhaseType;
    }

    public long getSecondsToPhase() {
        long currentTime = System.currentTimeMillis() / 1000;
        return getGameTimeOfPhase() - currentTime;
    }

    public String getReadableTimeToPhase() {
        TimeZone tz = TimeZone.getTimeZone("UTC");

        long secondsUntilPhase = getSecondsToPhase();
        /*
        Log.v("GameName", getGameName());
        Log.v("TimeOfPhase", String.valueOf(getGameTimeOfPhase()));
        Log.v("CurrentTime", String.valueOf(System.currentTimeMillis() / 1000));
        Log.v("SecondsToPhase", String.valueOf(secondsUntilPhase));
        */
        if (secondsUntilPhase > 0) {
            SimpleDateFormat df;
            if (secondsUntilPhase < 60) {
                df = new SimpleDateFormat("s's'");
            } else if (secondsUntilPhase < 3600) {
                df = new SimpleDateFormat("mm:ss");
            } else if (secondsUntilPhase < 86400) {
                df = new SimpleDateFormat("HH:mm:ss");
            } else {
                df = new SimpleDateFormat("d'd 'HH:mm:ss");
                secondsUntilPhase -= 86400; //not sure why this is necessary but it is
            }
            df.setTimeZone(tz);

            return df.format(new Date(secondsUntilPhase * 1000));
        } else {
            return "Now";
        }
    }
}
