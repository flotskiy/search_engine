package main.com.github.flotskiy.search.engine.controllers;

import main.com.github.flotskiy.search.engine.crawler.PageCrawlerTest;
import main.com.github.flotskiy.search.engine.indexing.Indexer;
import main.com.github.flotskiy.search.engine.model.Index;
import main.com.github.flotskiy.search.engine.repositories.FieldRepository;
import main.com.github.flotskiy.search.engine.repositories.IndexRepository;
import main.com.github.flotskiy.search.engine.repositories.LemmaRepository;
import main.com.github.flotskiy.search.engine.repositories.PageRepository;
import main.com.github.flotskiy.search.engine.util.FieldInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
        long start = System.currentTimeMillis();
        System.out.println("Processing started");
        new FieldInitializer(fieldRepository).init();
        PageCrawlerTest.testCrawler(pageRepository);
        System.out.println("Processing completed!");
        System.out.println("Время работы: " + (System.currentTimeMillis() - start) / 1000 + " секунд");
        Indexer indexer = new Indexer(pageRepository, fieldRepository, lemmaRepository, indexRepository);
        System.out.println("\nЧтение страниц из БД:\n");
        indexer.findAllPagesAndPrintIdAndPath();
    }
}
