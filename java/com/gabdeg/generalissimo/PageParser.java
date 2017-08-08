package com.gabdeg.generalissimo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class PageParser {

    Document page;

    public void loadFromStr(String html) {
        page = Jsoup.parse(html);
    }

    public void loadFromStr(String html, String baseUri) {
        page = Jsoup.parse(html, baseUri);
    }

    public Elements select(String query) {
        return page.select(query);
    }
}
