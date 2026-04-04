package com.dat.backend.movied.video.repository;

import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query(
            value = "select * from videos v where v.category = ?1 limit 3",
            nativeQuery = true
    )
    List<Video> findTop3VideoByCategory(Category category);

    @Query(
            value = "select * from videos v where v.category = ?1 and v.id != ?2",
            nativeQuery = true
    )
    List<Video> findRelateVideo(String category, Long videoId);

    List<Video> findAllByAuthorEmail(String authorEmail);
}
