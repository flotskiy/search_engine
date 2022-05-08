package com.github.flotskiy.search.engine.dataholders;

import com.github.flotskiy.search.engine.model.Lemma;
import com.github.flotskiy.search.engine.repositories.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    public Iterable<Lemma> getLemmasWithOccurrenceFrequencyPerCentMoreThan95() {
        return lemmaRepository.getLemmasWithOccurrenceFrequencyPerCentMoreThan95();
    }

    public Iterable<Lemma> getLemmasFromQueryWords(Set<String> queryWords) {
        return lemmaRepository.getLemmasWithQueryWords(queryWords);
    }

    public void truncateFields() {
        fieldRepository.truncateFields();
    }
}
