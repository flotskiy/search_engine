package com.github.flotskiy.search.engine.model.statistics;

import com.github.flotskiy.search.engine.model.Status;
import lombok.Data;

@Data
public class Detailed {
    private String url;
    private String name;
    private Status status;
    private long statusTime;
    private String error;
    private int pages;
    private int lemmas;
}
