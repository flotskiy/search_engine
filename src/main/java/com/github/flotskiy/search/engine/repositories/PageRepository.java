package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    @Query(
            value = "SELECT p.id, p.code, p.content, p.path, p.site_id " +
                    "FROM Pages p " +
                    "JOIN Search_index s " +
                    "ON p.id = s.page_id " +
                    "WHERE s.lemma_id = :lemmaId",
            nativeQuery = true
    )
    Iterable<Page> getPagesByLemmaId(@Param("lemmaId") int lemmaId);

    @Query(value = "SELECT * FROM Pages WHERE path = :path", nativeQuery = true)
    Page getPageByPath(@Param("path") String path);

    @Query(value = "SELECT COUNT(*) FROM Pages", nativeQuery = true)
    int getNumberOfPages();

    @Query(value = "SELECT COUNT(*) FROM Pages WHERE site_id = :siteId", nativeQuery = true)
    int getNumberOfPagesOnSite(@Param("siteId") int siteId);
}
