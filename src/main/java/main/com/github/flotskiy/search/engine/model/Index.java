package main.com.github.flotskiy.search.engine.model;

import javax.persistence.*;

@Entity
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "page_id", nullable = false)
    private int pageId;

    @Column(name = "lemma_id", nullable = false)
    private int lemmaId;


}
