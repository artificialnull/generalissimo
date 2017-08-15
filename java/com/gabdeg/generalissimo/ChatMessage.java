package com.gabdeg.generalissimo;


public class ChatMessage {

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderColor() {
        return senderColor;
    }

    public void setSenderColor(String senderColor) {
        this.senderColor = senderColor;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ChatMessage(String name, String color, String content) {
        this.senderName = name;
        this.senderColor = color;
        this.content = content;
    }

    public ChatMessage() {}

    public String toString() {
        return senderName + " (" + senderColor + ") -\n" + content;
    }

    String senderName;
    String senderColor;
    String content;

}
