package com.gabdeg.generalissimo;

import java.io.Serializable;

/**
 * Created by ishan on 8/14/17.
 */

public class Chat implements Serializable {
    public boolean canChat() {
        return canChat;
    }

    public void setChattiness(boolean canChat) {
        this.canChat = canChat;
    }

    public String getRand() {
        return rand;
    }

    public void setRand(String rand) {
        this.rand = rand;
    }

    private boolean canChat;
    private String rand;

    public boolean hasUnread() {
        return hasUnread;
    }

    public void setUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    private boolean hasUnread;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private String url;
}
