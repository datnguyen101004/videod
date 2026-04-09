package com.dat.backend.movied.video.repository;

import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {

    @Query(
            value = "select * from videos v where v.category = ?1 limit 3",
            nativeQuery = true
    )
    List<Video> findTop3VideoByCategory(Category category);

    Window<Video> findTop5ByCategoryAndIdNot(Category category, Long videoId, ScrollPosition scrollPosition, Sort sort);

    List<Video> findAllByAuthorEmail(String authorEmail);

    Window<Video> findTop9ByOrderByIdDescCreatedAtDesc(ScrollPosition scrollPosition);
}
