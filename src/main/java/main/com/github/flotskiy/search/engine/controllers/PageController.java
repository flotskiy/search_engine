package main.com.github.flotskiy.search.engine.controllers;

import main.com.github.flotskiy.search.engine.crawler.PageCrawlerTest;
import main.com.github.flotskiy.search.engine.repositories.PageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class PageController {

    @Autowired
    private PageRepository pageRepository;

    @GetMapping("/")
    public void start() throws IOException {
        long start = System.currentTimeMillis();
        System.out.println("Processing started");
        PageCrawlerTest.testCrawler(pageRepository);
        System.out.println("Processing completed!");
        System.out.println("Время работы: " + (System.currentTimeMillis() - start) / 1000 + " секунд");

    }
}
