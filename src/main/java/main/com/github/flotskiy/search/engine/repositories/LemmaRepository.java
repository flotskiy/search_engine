package main.com.github.flotskiy.search.engine.repositories;

import main.com.github.flotskiy.search.engine.model.Lemma;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    @Query(value = "SELECT id FROM Lemmas l WHERE l.lemma = :lemmaString LIMIT 1", nativeQuery = true)
    int getLemmaByName(@Param("lemmaString") String lemmaString);
}
