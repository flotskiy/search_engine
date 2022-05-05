package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.repositories.*;

public class RepositoriesHolder {
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;

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

    public PageRepository getPageRepository() {
        return pageRepository;
    }

    public FieldRepository getFieldRepository() {
        return fieldRepository;
    }

    public LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public SiteRepository getSiteRepository() {
        return siteRepository;
    }
}
