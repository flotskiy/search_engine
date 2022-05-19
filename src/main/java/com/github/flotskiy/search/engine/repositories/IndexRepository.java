package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Index;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {

    @Query(
            value = "SELECT SUM(lemma_rank) FROM Search_index WHERE page_id = :pageId AND lemma_id IN :lemmasList",
            nativeQuery = true
    )
    float getTotalLemmasRankForPage(@Param("pageId") int pageId, @Param("lemmasList") Collection<Integer> lemmasList);
}
