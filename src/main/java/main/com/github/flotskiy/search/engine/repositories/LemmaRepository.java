package main.com.github.flotskiy.search.engine.repositories;

import main.com.github.flotskiy.search.engine.model.Lemma;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {
}
