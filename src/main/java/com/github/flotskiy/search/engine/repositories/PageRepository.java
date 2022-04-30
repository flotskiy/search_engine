package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {

    @Query(
            value = "SELECT p.id, p.code, p.content, p.path " +
                    "FROM Pages p " +
                    "JOIN Search_index s " +
                    "ON p.id = s.page_id " +
                    "WHERE s.lemma_id = :lemmaId",
            nativeQuery = true
    )
    Iterable<Page> getPagesByLemmaId(@Param("lemmaId") int lemmaId);
}
