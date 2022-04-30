package com.github.flotskiy.search.engine.repositories;

import com.github.flotskiy.search.engine.model.Field;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends CrudRepository<Field, Integer> {
}
