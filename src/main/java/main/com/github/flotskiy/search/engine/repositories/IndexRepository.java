package main.com.github.flotskiy.search.engine.repositories;

import main.com.github.flotskiy.search.engine.model.Index;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
}
