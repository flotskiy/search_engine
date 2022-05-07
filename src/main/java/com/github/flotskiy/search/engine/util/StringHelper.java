package com.github.flotskiy.search.engine.util;

import com.github.flotskiy.search.engine.indexing.CollFiller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class StringHelper {
    private final BufferedReader BUFFERED_READER = new BufferedReader(new InputStreamReader(System.in));
    private final int SNIPPET_BORDER = 5;

    private final CollFiller collFiller;

    @Autowired
    public StringHelper(CollFiller collFiller) {
        this.collFiller = collFiller;
    }

    public String getInputString() {
        String input = "";
        System.out.println("Please type smth:");
        try {
            input = BUFFERED_READER.readLine();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return input;
    }

    public String cutProtocolAndHost(String pagePath, String homePage) {
        return pagePath.substring(homePage.length());
    }

    public boolean isHrefToPage(String href) {
        if (href.matches(".*(#|\\?).*")) {
            return false;
        }
        return !href.matches(
                ".*\\.(pdf|PDF|docx?|DOCX?|xlsx?|XLSX?|pptx?|PPTX?|jpe?g|JPE?G|gif|GIF|png|PNG" +
                        "|mp3|MP3|mp4|MP4|aac|AAC|json|JSON|csv|CSV|exe|EXE|apk|APK|rar|RAR|zip|ZIP" +
                        "|xml|XML|jar|JAR|bin|BIN|svg|SVG|nc|NC|webp|WEBP|m|M)/?"
        );
    }

    public boolean isHrefValid(String homePage, String href) {
        return href.startsWith(homePage) &&
                isHrefToPage(href) &&
                !collFiller.isPageAdded(href) &&
                !href.equals(homePage) &&
                !href.equals(homePage + "/");
    }

    public String buildSnippet(List<String> textList, List<Integer> lemmasPositions) {
        if (lemmasPositions.size() == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        int start = 0, end = -1;

        Map<Integer, Integer> snippetsBorders = lemmasPositions.stream()
                .collect(TreeMap::new, (map, i) -> map.put(i - SNIPPET_BORDER, i + SNIPPET_BORDER), Map::putAll);

        for (Map.Entry<Integer, Integer> entry : snippetsBorders.entrySet()) {
            if (entry.getKey() <= end) {
                end = entry.getValue();
                continue;
            }
            buildStringBuilder(builder, textList, lemmasPositions, start, end);
            start = entry.getKey();
            if (start < 0) {
                start = 0;
            }
            end = entry.getValue();
            if (end >= textList.size()) {
                end = textList.size() - 1;
            }
            if (isLastEntry(entry, lemmasPositions, SNIPPET_BORDER)) {
                buildStringBuilder(builder, textList, lemmasPositions, start, end);
            }
        }
        return builder.toString();
    }

    private boolean isLastEntry(Map.Entry<Integer, Integer> entry, List<Integer> lemmasPositions, int border) {
        return (entry.getValue() - border) == lemmasPositions.get(lemmasPositions.size() - 1);
    }

    private void buildStringBuilder(
            StringBuilder builder,
            List<String> textList,
            List<Integer> lemmasPositions,
            int start,
            int end
    ) {
        for (int i = start; i <= end; i++) {
            if (i == start) {
                builder.append("...");
            }
            if (lemmasPositions.contains(i)) {
                builder.append("<b>").append(textList.get(i)).append("<b>").append(" ");
            } else {
                builder.append(textList.get(i)).append(" ");
            }
            if (i == end) {
                builder.deleteCharAt(builder.length() - 1).append("...\n");
            }
        }
    }

    public String getHomePage(String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url.getProtocol() + "://" + url.getHost() + "/";
    }
}
