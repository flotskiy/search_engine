package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.indexing.PageCrawlerStarter;
import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;

@RestController
public class ManagementController {

    private final RepositoriesHolder repositoriesHolder;
    private final PageCrawlerStarter pageCrawlerStarter;

    @Autowired
    public ManagementController(
            RepositoriesHolder repositoriesHolder,
            PageCrawlerStarter pageCrawlerStarter
    ) {
        this.repositoriesHolder = repositoriesHolder;
        this.pageCrawlerStarter = pageCrawlerStarter;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<HashMap<String, Object>> start() throws IOException {
        HashMap<String, Object> response = new HashMap<>();
        if (!repositoriesHolder.isIndexing()) {
            new Thread(() -> pageCrawlerStarter.startCrawling(repositoriesHolder)).start();
            response.put("result", true);
            return ResponseEntity.ok().body(response);
        }
        response.put("result", false);
        response.put("error", "Индексация уже запущена");
        return ResponseEntity.badRequest().body(response);
    }
}
