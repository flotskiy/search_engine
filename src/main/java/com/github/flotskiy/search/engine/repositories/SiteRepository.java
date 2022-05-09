package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Site;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Sites WHERE name = :name AND id != :id", nativeQuery = true)
    void deletePreviouslyIndexedSiteByName(@Param("name") String name, @Param("id") int id);

    @Query(value = "SELECT * FROM Sites WHERE status = 'INDEXING'", nativeQuery = true)
    Iterable<Site> getIndexingSites();

    @Transactional
    @Modifying
    @Query(value = "UPDATE Sites SET status = :status, status_time = :statusTime WHERE id = :id", nativeQuery = true)
    void changeSiteStatus(@Param("id") int id, @Param("status") String status, @Param("statusTime") Date date);

    @Query(value = "SELECT * FROM Sites", nativeQuery = true)
    Iterable<Site> getAllSites();
}
