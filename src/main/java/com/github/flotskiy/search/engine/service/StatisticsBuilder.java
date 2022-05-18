package com.github.flotskiy.search.engine.service;

import com.github.flotskiy.search.engine.dataholders.RepositoriesHolder;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.statistics.Detailed;
import com.github.flotskiy.search.engine.model.statistics.Statistics;
import com.github.flotskiy.search.engine.model.statistics.Total;

import java.util.ArrayList;
import java.util.List;

public class StatisticsBuilder {

    public static Statistics build(RepositoriesHolder holder) {
        Statistics statistics = new Statistics();
        Total total = new Total();
        List<Detailed> detailed = new ArrayList<>();

        fillInTotal(holder, total);
        statistics.setTotal(total);
        fillInDetailed(holder, detailed);
        statistics.setDetailed(detailed);

        return statistics;
    }

    private static void fillInTotal(RepositoriesHolder holder, Total total) {
        total.setSites(getTotalSites(holder));
        total.setPages(getTotalPages(holder));
        total.setLemmas(getTotalLemmas(holder));
        total.setIndexing(isIndexing(holder));
    }

    private static void fillInDetailed(RepositoriesHolder holder, List<Detailed> detailed) {
        Iterable<Site> siteIterable = holder.getAllSites();
        for (Site site : siteIterable) {
            Detailed detailedObj = new Detailed();
            detailedObj.setUrl(site.getUrl());
            detailedObj.setName(site.getName());
            detailedObj.setStatus(site.getStatus());
            detailedObj.setStatusTime(site.getStatusTime().getTime());
            detailedObj.setError(site.getLastError());

            int pagesCount = holder.getPageRepository().getNumberOfPagesOnSite(site.getId());
            detailedObj.setPages(pagesCount);

            int lemmasCount = holder.getLemmaRepository().getNumberOfLemmasOnSite(site.getId());
            detailedObj.setLemmas(lemmasCount);

            detailed.add(detailedObj);
        }
    }

    private static int getTotalSites(RepositoriesHolder holder) {
        return holder.getSiteRepository().getNumberOfSites();
    }

    private static int getTotalPages(RepositoriesHolder holder) {
        return holder.getPageRepository().getNumberOfPages();
    }

    private static int getTotalLemmas(RepositoriesHolder holder) {
        return holder.getLemmaRepository().getNumberOfLemmas();
    }

    private static boolean isIndexing(RepositoriesHolder holder) {
        return holder.isIndexing();
    }
}
