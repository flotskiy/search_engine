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

    @Query(value = "SELECT * FROM Sites WHERE status IN ('INDEXING', 'FAILED')", nativeQuery = true)
    Iterable<Site> getIndexingAndFailedSites();

    @Transactional
    @Modifying
    @Query(value = "UPDATE Sites SET status = :status, status_time = :statusTime WHERE id = :id", nativeQuery = true)
    void setSiteStatus(@Param("id") int id, @Param("status") String status, @Param("statusTime") Date date);

    @Transactional
    @Modifying
    @Query(
            value = "UPDATE Sites SET last_error = :error, status = 'FAILED', status_time = :statusTime WHERE id = :id",
            nativeQuery = true
    )
    void setFailedStatus(@Param("id") int id, @Param("statusTime") Date date, @Param("error") String error);

    @Query(value = "SELECT COUNT(*) FROM Sites", nativeQuery = true)
    int getNumberOfSites();
}
