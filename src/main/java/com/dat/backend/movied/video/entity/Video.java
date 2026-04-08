package com.dat.backend.movied.video.entity;

import com.dat.backend.movied.common.entity.TimeBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "videos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video extends TimeBaseEntity {
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
