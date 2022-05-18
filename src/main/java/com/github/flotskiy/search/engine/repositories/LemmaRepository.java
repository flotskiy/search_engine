package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT * FROM Lemmas l " +
            "WHERE l.lemma IN :lemmasList " +
            "AND l.frequency < (SELECT COUNT(*) * 95 / 100 FROM Pages p WHERE p.site_id = l.site_id)",
            nativeQuery = true)
    Iterable<Lemma> getLemmasWithQueryWordsAndWithOccurrenceFrequencyPerCentLessThan95(
            @Param("lemmasList") Collection<String> lemmasList
    );

    @Query(value = "SELECT COUNT(*) FROM Lemmas", nativeQuery = true)
    int getNumberOfLemmas();

    @Query(value = "SELECT COUNT(*) FROM Lemmas WHERE site_id = :siteId", nativeQuery = true)
    int getNumberOfLemmasOnSite(@Param("siteId") int siteId);

    @Query(value = "SELECT * FROM Lemmas WHERE site_id = :siteId", nativeQuery = true)
    Iterable<Lemma> getAllLemmasFromSite(@Param("siteId") int siteId);
}
