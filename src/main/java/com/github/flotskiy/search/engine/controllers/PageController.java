package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.crawler.PageCrawlerTest;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import com.github.flotskiy.search.engine.repositories.IndexRepository;
import com.github.flotskiy.search.engine.search.QueryHandler;
import com.github.flotskiy.search.engine.search.SearchResultPage;
import com.github.flotskiy.search.engine.repositories.FieldRepository;
import com.github.flotskiy.search.engine.repositories.LemmaRepository;
import com.github.flotskiy.search.engine.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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
        long start = System.currentTimeMillis();
        RepoFiller.fillInFieldsTable(repositoriesHolder);
        PageCrawlerTest.testCrawler(repositoriesHolder);
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
        System.out.println("\nQueryHandler test\n");
        List<SearchResultPage> searchResultPageList = QueryHandler.getSearchResult(repositoriesHolder);
        searchResultPageList.forEach(System.out::println);
    }
}
