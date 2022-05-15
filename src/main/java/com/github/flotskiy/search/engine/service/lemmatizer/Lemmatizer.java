package com.github.flotskiy.search.engine.service.lemmatizer;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Lemmatizer {

    private static final String SERVICE_PARTS_OF_SPEECH = ".*(ПРЕДЛ|СОЮЗ|ЧАСТ|МЕЖД)$";
    private static LuceneMorphology luceneMorphology;

    static {
        try {
            luceneMorphology = new RussianLuceneMorphology();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Integer> getLemmasCountMap(String text) {
        Map<String, Integer> lemmasCountMap = new HashMap<>();
        for (String word : getWordsWithoutServicePartsOfSpeech(text)) {
            for (String wordNormalForm : luceneMorphology.getNormalForms(word)) {
                lemmasCountMap.put(wordNormalForm, lemmasCountMap.getOrDefault(wordNormalForm, 0) + 1);
            }
        }
        return lemmasCountMap;
    }

    private static List<String> getWordsWithoutServicePartsOfSpeech(String text) {
         return Arrays.stream((text).split("[^а-яёА-ЯЁ]+"))
                 .filter(word -> word.length() != 0)
                 .map(String::toLowerCase)
                 .filter(word -> luceneMorphology
                         .getMorphInfo(word)
                         .stream()
                         .noneMatch(baseFormWord -> baseFormWord.matches(SERVICE_PARTS_OF_SPEECH)))
                 .collect(Collectors.toList());
    }

    public static List<String> getLemmatizedList(List<String> list) {
        return list
                .stream()
                .map(s -> s = s.matches("[^(.+)?[а-яёА-ЯЁ]+(.+)?]") ? "в" : s)
                .map(String::toLowerCase)
                .map(Lemmatizer::getCirillicWord)
                .map(s -> s = s.length() < 1 ? "в" : s)
                .map(s -> s = luceneMorphology.getMorphInfo(s).toString().matches(SERVICE_PARTS_OF_SPEECH) ?
                                        "" : luceneMorphology.getNormalForms(s).get(0))
                .collect(Collectors.toList());
    }

    private static String getCirillicWord(String word) {
        return word.replaceAll("[^а-яё]", "");
    }
}
