package main.com.github.flotskiy.search.engine.controllers;

import main.com.github.flotskiy.search.engine.model.Lemma;
import main.com.github.flotskiy.search.engine.model.Page;
import main.com.github.flotskiy.search.engine.repositories.FieldRepository;
import main.com.github.flotskiy.search.engine.repositories.IndexRepository;
import main.com.github.flotskiy.search.engine.repositories.LemmaRepository;
import main.com.github.flotskiy.search.engine.repositories.PageRepository;
import main.com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import main.com.github.flotskiy.search.engine.search.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

@RestController
public class PageController {

    @Autowired
    private PageRepository pageRepository;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private LemmaRepository lemmaRepository;

    @Autowired
    private IndexRepository indexRepository;

    @GetMapping("/")
    public void start() throws IOException {
        RepositoriesHolder repositoriesHolder =
                new RepositoriesHolder(pageRepository, fieldRepository, lemmaRepository, indexRepository);
//        long start = System.currentTimeMillis();
//        RepoFiller.fillInFieldsTable(repositoriesHolder);
//        PageCrawlerTest.testCrawler(repositoriesHolder);
//        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
        Iterable<Lemma> lemmas =
                repositoriesHolder.getLemmaRepository().getLemmasWithOccurrenceFrequencyPerCentMoreThan95();
        lemmas.forEach(lemma -> System.out.println(lemma.getLemma() + " - " + lemma.getFrequency() + " - " + lemma));
        System.out.println(" ----- ");
        Set<String> queryList = Set.of("банк", "мамт", "нгпу", "экспорт", "иметь", "тот", "он");
        Iterable<Lemma> queryLemmas = repositoriesHolder.getLemmaRepository().getLemmasWithQueryWords(queryList);
        queryLemmas.forEach(lemma -> System.out.println(lemma.getLemma() + " - " + lemma.getFrequency() + " - " + lemma));

        System.out.println("\nQueryHandler test\n");
        QueryHandler handler = new QueryHandler(repositoriesHolder);
        Set<Page> pages = handler.getPagesSet();
        System.out.println("\nretainAll for sets testing");
        pages.forEach(p ->System.out.println(p.getId() + " - " + p.getPath()));
    }
}
