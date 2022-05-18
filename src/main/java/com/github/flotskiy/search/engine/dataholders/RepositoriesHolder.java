package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.model.Lemma;
import com.github.flotskiy.search.engine.model.Page;
import com.github.flotskiy.search.engine.model.Site;
import com.github.flotskiy.search.engine.model.Status;
import com.github.flotskiy.search.engine.repositories.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

@Getter
@Component
public class RepositoriesHolder {
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

    @Autowired
    public RepositoriesHolder(
            PageRepository pageRepository,
            FieldRepository fieldRepository,
            LemmaRepository lemmaRepository,
            IndexRepository indexRepository,
            SiteRepository siteRepository
    ) {
        this.pageRepository = pageRepository;
        this.fieldRepository = fieldRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
    }

    public Iterable<Lemma> getLemmasWithQueryWords(Set<String> queryWords) {
        return lemmaRepository.getLemmasWithQueryWords(queryWords);
    }

    public void truncateFields() {
        fieldRepository.truncateFields();
    }

    public boolean isIndexing() {
        Iterable<Site> siteIterable = siteRepository.getIndexingSites();
        return siteIterable.iterator().hasNext();
    }

    public Iterable<Site> getIndexingAndFailedSites() {
        return siteRepository.getIndexingAndFailedSites();
    }

    public void setSiteStatus(int id, Status status) {
        siteRepository.setSiteStatus(id, status.toString(), new Date());
    }

    public void setFailedStatus(int id, String error) {
        siteRepository.setFailedStatus(id, new Date(), error);
    }

    public Iterable<Page> getPagesByLemmaAndSiteId(String lemma, int siteId) {
        if (siteId == -1) {
            return pageRepository.getPagesByLemma(lemma);
        }
        return pageRepository.getPagesByLemmaAndSiteId(lemma, siteId);
    }

    public float get95perCentPagesCount(int siteId) {
        return pageRepository.get95perCentPagesCount(siteId);
    }

    public Iterable<Site> getAllSites() {
        return siteRepository.getAllSites();
    }
}
