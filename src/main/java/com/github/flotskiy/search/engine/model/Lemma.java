package com.github.flotskiy.search.engine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Lemmas")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String lemma;

    @Column(nullable = false)
    private int frequency;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site siteId;

    @OneToMany(mappedBy = "lemmaId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Index> indexes;

    public Lemma(String lemma, int frequency, Site siteId) {
        this.lemma = lemma;
        this.frequency = frequency;
        this.siteId = siteId;
    }
}
