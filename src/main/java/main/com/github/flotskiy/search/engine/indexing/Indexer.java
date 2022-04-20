package main.com.github.flotskiy.search.engine.indexing;

import main.com.github.flotskiy.search.engine.lemmatizer.Lemmatizer;
import main.com.github.flotskiy.search.engine.model.Field;
import main.com.github.flotskiy.search.engine.model.Index;
import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.repositories.FieldRepository;
import main.com.github.flotskiy.search.engine.repositories.IndexRepository;
import main.com.github.flotskiy.search.engine.repositories.LemmaRepository;
import main.com.github.flotskiy.search.engine.repositories.PageRepository;
import main.com.github.flotskiy.search.engine.util.TempIndex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Indexer {

    private PageRepository pageRepository;
    private FieldRepository fieldRepository;
    private LemmaRepository lemmaRepository;
    private IndexRepository indexRepository;

    private Map<String, Float> selectorsAndWeight = new HashMap<>();
    private Map<String, Integer> lemmasMap = new HashMap<>();

    private List<Lemma> lemmasList = new ArrayList<>();

    private List<TempIndex> tempIndexList = new ArrayList<>();

    public Indexer(
            PageRepository pageRepository,
            FieldRepository fieldRepository,
            LemmaRepository lemmaRepository,
            IndexRepository indexRepository
    ) {
        this.pageRepository = pageRepository;
        this.fieldRepository = fieldRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        fillInSelectorsAndWeigh();
    }

    private void fillInSelectorsAndWeigh() {
        Iterable<Field> fieldIterable = fieldRepository.findAll();
        for (Field field : fieldIterable) {
            selectorsAndWeight.put(field.getSelector(), field.getWeight());
        }
    }

    public void findAllPagesAndPrintIdAndPath() {
        Iterable<Page> pageIterable = getAllPages();
        for (Page page : pageIterable) {
            System.out.println("\n\n" + page.getId() + " - " + page.getPath() + "\n");
            getInfoFromPage(page);
        }

        System.out.println("Размер lemmasMap - " + lemmasMap.size());
        for (String key : lemmasMap.keySet()) {
            Lemma lemma = new Lemma(key, lemmasMap.get(key));
            lemmasList.add(lemma);
        }
        lemmaRepository.saveAll(lemmasList);

        fillInIndex();
    }

    private void fillInIndex() {
        List<Index> indexList = new ArrayList<>();

        for (TempIndex tempIndex : tempIndexList) {
            int lemmaId = lemmaRepository.getLemmaByName(tempIndex.getLemma());
            Lemma lemma = lemmaRepository.findById(lemmaId).orElseThrow();
            System.out.println("Lemma test in fillInIndex method - "
                    + lemma.getLemma() + " - " + lemma.getFrequency());
            indexList.add(new Index(tempIndex.getPage(), lemma, tempIndex.getLemmaRank()));
        }

        System.out.println("tempIndexList.size() - " + tempIndexList.size());
        System.out.println("indexList.size() - " + indexList.size());
        indexRepository.saveAll(indexList);
    }

    private Iterable<Page> getAllPages() {
        return pageRepository.findAll();
    }

    private void getInfoFromPage(Page page) {
        if (page.getCode() != 200) {
            return;
        }

        Document document = Jsoup.parse(page.getContent());

        String title = document.title();
        System.out.println(title);
        Map<String, Integer> titleLemmasCount = Lemmatizer.getLemmasCountMap(title);
        System.out.println(titleLemmasCount);

        String bodyText = document.body().text();
        System.out.println("Body text: " + bodyText);
        Map<String, Integer> bodyLemmasCount = Lemmatizer.getLemmasCountMap(bodyText);
        System.out.println(bodyLemmasCount);

        Map<String, Integer> uniqueLemmasInTitleAndBody = Stream
                        .concat(titleLemmasCount.entrySet().stream(), bodyLemmasCount.entrySet().stream())
                        .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingInt(Map.Entry::getValue)));

        System.out.println("Все леммы: " + uniqueLemmasInTitleAndBody);

        float lemmaRank;

        for (String lemma : uniqueLemmasInTitleAndBody.keySet()) {
            lemmasMap.put(lemma, lemmasMap.getOrDefault(lemma, 0) + 1);
            lemmaRank = titleLemmasCount.getOrDefault(lemma, 0) * selectorsAndWeight.get("title") +
                            bodyLemmasCount.getOrDefault(lemma, 0) * selectorsAndWeight.get("body");
            tempIndexList.add(new TempIndex(page, lemma, lemmaRank));
        }
    }
}
