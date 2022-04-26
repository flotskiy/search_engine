package main.com.github.flotskiy.search.engine.repositories;

import main.com.github.flotskiy.search.engine.model.Page;
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
    Iterable<Page> getPagesByFirstLemmaId(@Param("lemmaId") int lemmaId);

    @Query(
            value = "SELECT p.id as pid, p.code, p.content, p.path, s.id as sid, s.lemma_rank, s.lemma_id " +
                    "FROM Pages p " +
                    "JOIN Search_index s " +
                    "ON p.id = s.page_id " +
                    "WHERE s.lemma_id = :lemmaId",
            nativeQuery = true
    )
    Iterable<Object[]> getPagesJoinedWithIndex(@Param("lemmaId") int lemmaId);
}
