package main.com.github.flotskiy.search.engine.util;

import main.com.github.flotskiy.search.engine.model.Field;
import main.com.github.flotskiy.search.engine.repositories.FieldRepository;

public class FieldInitializer {

    private final FieldRepository fieldRepository;

    public FieldInitializer(FieldRepository fieldRepository) {
        this.fieldRepository = fieldRepository;
    }

    public void init() {
        fieldRepository.save(new Field("title", "title", 1.0f));
        fieldRepository.save(new Field("body", "body", 0.8f));
    }
}
