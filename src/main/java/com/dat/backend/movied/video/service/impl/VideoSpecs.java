package com.dat.backend.movied.video.service.impl;

import com.dat.backend.movied.video.entity.Category;
import com.dat.backend.movied.video.entity.Video;
import com.dat.backend.movied.video.entity.Video_;
import org.springframework.data.jpa.domain.PredicateSpecification;

import java.time.Instant;

public class VideoSpecs {
    static PredicateSpecification<Video> afterAt(Instant after) {
        return (from, cb) -> {
            return cb.greaterThanOrEqualTo(from.get(Video_.createdAt), after);
        };
    }

    static PredicateSpecification<Video> beforeAt(Instant before) {
        return (from, cb) -> {
            return cb.lessThanOrEqualTo(from.get(Video_.createdAt), before);
        };
    }

    static PredicateSpecification<Video> titleLike(String title) {
        return (from, cb) -> {
          return cb.like(from.get(Video_.title), "%" + title + "%");
        };
    }

    static PredicateSpecification<Video> descriptionLike(String description) {
        return (from, cb) -> {
            return cb.like(from.get(Video_.description),"%" + description + "%");
        };
    }

    static PredicateSpecification<Video> categoryEqual(Category category) {
        return (from, cb) -> {
            return cb.equal(from.get(Video_.category), category);
        };
    }
}
