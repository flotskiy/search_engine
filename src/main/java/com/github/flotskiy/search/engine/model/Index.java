package com.github.flotskiy.search.engine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "search_index")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id", nullable = false)
    private Page pageId;

    @ManyToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id", nullable = false)
    private Lemma lemmaId;

    @Column(name = "lemma_rank", nullable = false)
    private float lemmaRank;

    public Index(Page pageId, Lemma lemmaId, float lemmaRank) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        this.lemmaRank = lemmaRank;
    }
}
