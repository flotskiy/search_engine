package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Site;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    @Transactional
    @Modifying
    @Query(
            value = "DELETE FROM Sites WHERE name = :name AND id != :id",
            nativeQuery = true
    )
    int deletePreviouslyIndexedSiteByName(@Param("name") String name, @Param("id") int id);
}
