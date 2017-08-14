package com.gabdeg.generalissimo;



public class NationChatPair {

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    Nation nation;
    Chat chat;

    public NationChatPair(Nation n, Chat c) {
        nation = n;
        chat = c;
    }
}
