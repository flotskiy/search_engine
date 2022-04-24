package main.com.github.flotskiy.search.engine.util;

import main.com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import main.com.github.flotskiy.search.engine.indexing.CollFiller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StringHelper {
    private static final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));

    public static String getPath() {
        String input = "";
        System.out.println("Please input site url:");
        try {
            input = BUFFERED_READER.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return input;
    }

    public static String cutProtocolAndHost(String pagePath, String homePage) {
        return pagePath.substring(homePage.length());
    }

    public static boolean isHrefToPage(String href) {
        if (href.matches(".*(#|\\?).*")) {
            return false;
        }
        return !href.matches(
                ".*\\.(pdf|PDF|docx?|DOCX?|xlsx?|XLSX?|pptx?|PPTX?|jpe?g|JPE?G|gif|GIF|png|PNG" +
                        "|mp3|MP3|mp4|MP4|aac|AAC|json|JSON|csv|CSV|exe|EXE|apk|APK|rar|RAR|zip|ZIP" +
                        "|xml|XML|jar|JAR|bin|BIN|svg|SVG|nc|NC|webp|WEBP)/?"
        );
    }

    public static boolean isHrefValid(CollectionsHolder holder, String homePage, String href) {
        return href.startsWith(homePage) &&
                StringHelper.isHrefToPage(href) &&
                !CollFiller.isPageAdded(holder, href) &&
                !href.equals(homePage) &&
                !href.equals(homePage + "/");
    }
}
