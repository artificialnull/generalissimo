package com.gabdeg.generalissimo;

/**
 * Created by ishan on 8/14/17.
 */

public class Chat {
    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public boolean isCanChat() {
        return canChat;
    }

    public void setCanChat(boolean canChat) {
        this.canChat = canChat;
    }

    public String getRand() {
        return rand;
    }

    public void setRand(String rand) {
        this.rand = rand;
    }

    private Nation nation;
    private boolean canChat;
    private String rand;
}
