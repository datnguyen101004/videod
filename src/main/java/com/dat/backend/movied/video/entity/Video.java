package com.dat.backend.movied.video.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String title;
    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column
    private String url;

    @Column(
            name = "author_id"
    )
    private String authorId;

}
