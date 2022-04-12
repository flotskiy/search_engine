package main.com.github.flotskiy.search.engine.lemmatizer;

import java.io.IOException;
import java.util.Map;

public class LemmatizerMainTest {

    public static void main(String[] args) throws IOException {

        String inputText =
                "из над под э эх или и за от не под между к же ох ой ах эй ай " +
                "Предлог — служебная часть речи, обозначающая отношение между объектом и субъектом, " +
                "выражающая синтаксическую зависимость имен существительных, местоимений, числительных " +
                "от других слов в словосочетаниях и предложениях. Предлоги, как и все служебные слова, " +
                "не могут употребляться самостоятельно, они всегда относятся к какому-нибудь существительному " +
                "(или слову, употребляемому в функции существительного).";

        Map<String, Integer> testMap = Lemmatizer.getLemmasCountMap(inputText);
        testMap.entrySet().forEach(System.out::println);
    }
}
