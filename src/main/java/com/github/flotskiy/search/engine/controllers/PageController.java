package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.crawler.PageCrawlerStarter;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import com.github.flotskiy.search.engine.repositories.*;
import com.github.flotskiy.search.engine.search.QueryHandler;
import com.github.flotskiy.search.engine.search.SearchResultPage;
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

    @Autowired
    private SiteRepository siteRepository;

    @GetMapping("/")
    public void start() throws IOException {
        RepositoriesHolder repositoriesHolder =
                new RepositoriesHolder(pageRepository, fieldRepository, lemmaRepository, indexRepository, siteRepository);
        long start = System.currentTimeMillis();
        RepoFiller.fillInFields(repositoriesHolder);
        PageCrawlerStarter.testCrawler(repositoriesHolder);
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
        System.out.println("\nQueryHandler test\n");
        List<SearchResultPage> searchResultPageList = QueryHandler.getSearchResult(repositoriesHolder);
        searchResultPageList.forEach(System.out::println);
    }
}
