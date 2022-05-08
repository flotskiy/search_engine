package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Field;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FieldRepository extends CrudRepository<Field, Integer> {

    @Transactional
    @Modifying
    @Query(value = "TRUNCATE Fields", nativeQuery = true)
    void truncateFields();
}
