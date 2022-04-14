package main.com.github.flotskiy.search.engine.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.persistence.Index;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Pages", indexes = @Index(name = "path_index", columnList = "path"))
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    public Page(String path, int code, String content) {
        this.path = path;
        this.code = code;
        this.content = content;
    }
}
