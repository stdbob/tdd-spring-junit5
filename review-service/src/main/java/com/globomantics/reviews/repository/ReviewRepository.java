package com.globomantics.reviews.repository;

import com.globomantics.reviews.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

  Optional<Review> findByProductId(Integer productId);
}
