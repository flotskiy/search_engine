package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.indexing.CollFiller;
import com.github.flotskiy.search.engine.indexing.PageCrawlerStarter;
import com.github.flotskiy.search.engine.indexing.RepoFiller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class ManagementController {

    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;

    private PageCrawlerStarter pageCrawlerStarter;

    @Autowired
    public ManagementController(CollectionsHolder collectionsHolder, RepoFiller repoFiller, CollFiller collFiller) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<HashMap<String, Object>> startIndexing() throws IOException {
        pageCrawlerStarter = new PageCrawlerStarter(collectionsHolder, repoFiller, collFiller);
        HashMap<String, Object> response = new HashMap<>();
        if (!pageCrawlerStarter.isStopped()) {
            new Thread(pageCrawlerStarter::startCrawling).start();
            response.put("result", true);
            return ResponseEntity.ok().body(response);
        }
        response.put("result", false);
        response.put("error", "Индексация уже запущена");
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<HashMap<String, Object>> stopIndexing() throws IOException {
        HashMap<String, Object> response = new HashMap<>();
        if (!pageCrawlerStarter.isStopped()) {
            pageCrawlerStarter.stopIndexing();
            response.put("result", true);
            return ResponseEntity.ok().body(response);
        }
        response.put("result", false);
        response.put("error", "Индексация не запущена");
        return ResponseEntity.badRequest().body(response);
    }
}
