package com.github.flotskiy.search.engine.controllers;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.statistics.Statistics;
import com.github.flotskiy.search.engine.service.StatisticsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;

@Controller
public class DashboardController {

    private final RepositoriesHolder repositoriesHolder;

    @Autowired
    public DashboardController(RepositoriesHolder repositoriesHolder) {
        this.repositoriesHolder = repositoriesHolder;
    }

    @GetMapping("${url}")
    public String index() {
        return "index";
    }

    @GetMapping("/statistics")
    public ResponseEntity<HashMap<String, Object>> getStatistics() {
        HashMap<String, Object> response = new HashMap<>();
        Statistics statistics = StatisticsBuilder.build(repositoriesHolder);
        response.put("result", true);
        response.put("statistics", statistics);
        return ResponseEntity.ok().body(response);
    }
}
