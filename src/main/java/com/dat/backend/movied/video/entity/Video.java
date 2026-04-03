package com.dat.backend.movied.video.entity;

import jakarta.persistence.*;
import lombok.*;
import software.amazon.awssdk.annotations.NotNull;

@Entity(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Category category;

    @Column
    private String keyStorage;

    @Column
    private String url;

    @Column(
            name = "author_email"
    )
    private String authorEmail;

}
