package main.com.github.flotskiy.search.engine.dataholders;

import main.com.github.flotskiy.search.engine.repositories.FieldRepository;
import main.com.github.flotskiy.search.engine.repositories.IndexRepository;
import main.com.github.flotskiy.search.engine.repositories.LemmaRepository;
import main.com.github.flotskiy.search.engine.repositories.PageRepository;

public class RepositoriesHolder {
    private final PageRepository pageRepository;
    private final FieldRepository fieldRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public RepositoriesHolder(
            PageRepository pageRepository,
            FieldRepository fieldRepository,
            LemmaRepository lemmaRepository,
            IndexRepository indexRepository
    ) {
        this.pageRepository = pageRepository;
        this.fieldRepository = fieldRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
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
}
