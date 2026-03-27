package com.dat.backend.movied.video.unit_test;

import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.repository.VideoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

@DataJpaTest
public class VideoRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;

    @BeforeEach
    void setUp() {
        videoRepository.save(createVideo("Video 1", "education"));
        videoRepository.save(createVideo("Video 2", "education"));
        videoRepository.save(createVideo("Video 3", "education"));
        videoRepository.save(createVideo("Video 4", "music"));
        videoRepository.save(createVideo("Video 5", "education"));
    }

    private Video createVideo(String title, String category) {
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(title);
        video.setCategory(Category.valueOf(category.toUpperCase()));
        return video;
    }

    @Test
    void testFindTop3VideoByCategory() {
        List<Video> result = videoRepository.findTop3VideoByCategory(Category.EDUCATION);

        Assertions.assertEquals(3, result.size());
        /*for (Video video : result) {
            Assertions.assertEquals("EDUCATION", video.getCategory().toString());
        }*/

        Assertions.assertTrue(
                result.stream()
                        .allMatch(v -> v.getCategory() == Category.EDUCATION)
        );
    }
}
