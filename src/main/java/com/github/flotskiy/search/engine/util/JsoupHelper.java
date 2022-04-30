package com.github.flotskiy.search.engine.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupHelper {

    public static Document getDocument(String stringToParse) {
        return Jsoup.parse(stringToParse);
    }

    public static String getTitle(Document document) {
        return document.title();
    }
}
