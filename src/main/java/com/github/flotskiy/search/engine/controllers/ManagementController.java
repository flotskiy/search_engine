package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.indexing.PageCrawlerStarter;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import com.github.flotskiy.search.engine.search.QueryHandler;
import com.github.flotskiy.search.engine.search.SearchResultPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class ManagementController {

    private final RepositoriesHolder repositoriesHolder;
    private final RepoFiller repoFiller;
    private final PageCrawlerStarter pageCrawlerStarter;
    private final QueryHandler queryHandler;

    @Autowired
    public ManagementController(
            RepositoriesHolder repositoriesHolder,
            RepoFiller repoFiller,
            PageCrawlerStarter pageCrawlerStarter,
            QueryHandler queryHandler
    ) {
        this.repositoriesHolder = repositoriesHolder;
        this.repoFiller = repoFiller;
        this.pageCrawlerStarter = pageCrawlerStarter;
        this.queryHandler = queryHandler;
    }

    @GetMapping("/startIndexing")
    public void start() throws IOException {
        long start = System.currentTimeMillis();
        repoFiller.fillInFields();
        pageCrawlerStarter.makeCrawling(repositoriesHolder);
        System.out.println("Duration of processing: " + (System.currentTimeMillis() - start) / 1000 + " s");
        List<SearchResultPage> searchResultPageList = queryHandler.getSearchResult();
        searchResultPageList.forEach(System.out::println);
    }
}
