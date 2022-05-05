package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Site;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {
}
