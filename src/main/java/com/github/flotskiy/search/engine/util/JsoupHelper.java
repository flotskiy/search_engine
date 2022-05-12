package com.github.flotskiy.search.engine.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class JsoupHelper {

    public static Connection getConnection(String pagePath) {
        return Jsoup.connect(pagePath)
                .userAgent(YmlConfigGetter.getConnectUseragent())
//                    .userAgent("FlotskiySearchBot")
                .referrer(YmlConfigGetter.getConnectReferrer())
                .ignoreHttpErrors(true);
    }

    public static Document getDocument(String stringToParse) {
        return Jsoup.parse(stringToParse);
    }

    public static String getTitle(Document document) {
        return document.title();
    }
}
