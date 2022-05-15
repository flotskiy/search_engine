package com.github.flotskiy.search.engine.service.lemmatizer;

import com.github.flotskiy.search.engine.util.StringHelper;

import java.io.IOException;
import java.util.*;

public class LemmatizerMainTest {

    public static void main(String[] args) throws IOException {

        String inputText =
                "из над под э эх или и за от не под между к же ох ой ах эй ай " +
                "Предлог — служебная часть речи, обозначающая отношение между объектом и субъектом, " +
                "выражающая синтаксическую зависимость имен существительных, местоимений, числительных " +
                "от других слов в словосочетаниях и предложениях. Предлоги, как и все служебные слова, " +
                "не могут употребляться самостоятельно, они всегда относятся к какому-нибудь существительному " +
                "(или слову, употребляемому в функции существительного).";

        Map<String, Integer> testMap = Lemmatizer.getLemmasCountMap("800 добрых дел | Нижний 800");
        testMap.entrySet().forEach(System.out::println);
        List<String> textList = new ArrayList<>(Arrays.asList(inputText.split("\\s+")));
        System.out.println("textList test size: " + textList.size());
        textList.stream().forEach(System.out::println);
        List<String> textListLemmatized = Lemmatizer.getLemmatizedList(textList);
        System.out.println("textListLemmatized test size: " + textListLemmatized.size());
        textListLemmatized.stream().forEach(System.out::println);
        System.out.println("-------");
        Map<Integer, String> textMapLemmatized =
                textListLemmatized.stream().collect(HashMap::new, (map, s) -> map.put(map.size(), s), Map::putAll);
        List<String> queryList = List.of("предлог", "слово", "функции", "предложение", "других");
        Map<Integer, String> filteredMap = textMapLemmatized.entrySet().stream()
                .filter(e -> {
                    for (String queryWord : queryList) {
                        if (queryWord.equals(e.getValue())) {
                            return true;
                        }
                    }
                    return false;
                }).collect(HashMap::new, (map, e) -> map.put(e.getKey(), e.getValue()), Map::putAll);
        System.out.println("\ntextMapLemmatized.forEach((k, v) -> System.out.println(k + \" - \" + v));");
        textMapLemmatized.forEach((k, v) -> System.out.println(k + " - " + v));
        System.out.println("\nfilteredMap.forEach((k, v) -> System.out.println(k + \" - \" + v));");
        filteredMap.forEach((k, v) -> System.out.println(k + " - " + v));
        List<Integer> lemmasPositions = new ArrayList<>(filteredMap.keySet());
        lemmasPositions.sort(Integer::compareTo);
        lemmasPositions.forEach(System.out::println);
        Map<Integer, Integer> snippetsBorders = lemmasPositions.stream()
                .collect(TreeMap::new, (map, i) -> map.put(i - 5, i + 5), Map::putAll);
        snippetsBorders.forEach((k, v) -> System.out.println(k + " - " + v));
        String snippet = StringHelper.buildSnippet(textList, lemmasPositions);
        System.out.println(snippet);
    }
}
