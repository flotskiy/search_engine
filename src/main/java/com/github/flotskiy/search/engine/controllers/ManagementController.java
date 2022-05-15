package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.dataholders.CollectionsHolder;
import com.github.flotskiy.search.engine.service.indexing.all.CollFiller;
import com.github.flotskiy.search.engine.service.indexing.all.PageCrawlerStarter;
import com.github.flotskiy.search.engine.service.indexing.all.RepoFiller;
import com.github.flotskiy.search.engine.service.indexing.one.SinglePageCrawler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class ManagementController {

    private final CollectionsHolder collectionsHolder;
    private final RepoFiller repoFiller;
    private final CollFiller collFiller;
    private final SinglePageCrawler singlePageCrawler;

    private PageCrawlerStarter pageCrawlerStarter;

    @Autowired
    public ManagementController(
            CollectionsHolder collectionsHolder,
            RepoFiller repoFiller,
            CollFiller collFiller,
            SinglePageCrawler singlePageCrawler
    ) {
        this.collectionsHolder = collectionsHolder;
        this.repoFiller = repoFiller;
        this.collFiller = collFiller;
        this.singlePageCrawler = singlePageCrawler;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<HashMap<String, Object>> startIndexing() {
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
    public ResponseEntity<HashMap<String, Object>> stopIndexing() {
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

    @PostMapping("/indexPage")
    public ResponseEntity<HashMap<String, Object>> indexPage(@RequestParam(name="url", required=false) String path) {
        HashMap<String, Object> response = new HashMap<>();
        if (singlePageCrawler.addOrUpdateSinglePage(path)) {
            response.put("result", true);
            return ResponseEntity.ok().body(response);
        }
        response.put("result", false);
        response.put("error", "Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        return ResponseEntity.badRequest().body(response);
    }
}
