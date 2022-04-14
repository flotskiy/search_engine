package main.com.github.flotskiy.search.engine.repositories;

import main.com.github.flotskiy.search.engine.model.Page;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
}
