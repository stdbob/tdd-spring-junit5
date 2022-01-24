package com.globomantics.reviews.controller;

import com.globomantics.reviews.model.Review;
import com.globomantics.reviews.model.ReviewEntry;
import com.globomantics.reviews.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

/**
 * A RestController that manages product reviews.
 */
@RestController
@Slf4j
@RequestMapping(ReviewController.REQUEST_MAPPING)
public class ReviewController {

  public static final String REQUEST_MAPPING = "/reviews";

  private ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  /**
   * Returns the review with the specified ID.
   * @param id    The ID of the review to return.
   * @return      The review with the specified ID, or 404 Not Found.
   */
  @GetMapping("/{id}")
  public ResponseEntity<?> getReview(@PathVariable String id) {
    return service.findById(id)
        .map(review -> {
          try {
            return ResponseEntity
                .ok()
                .eTag(Integer.toString(review.getVersion()))
                .location(new URI(REQUEST_MAPPING +"/" + review.getId()))
                .body(review);
          } catch (URISyntaxException e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
          }
        })
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Returns either all reviews or the review for the specified productId.
   * @param productId The productId for the review to return. This request parameter is optional, if it is omitted
   *                  then all reviews are returned.
   * @return          A list of reviews.
   */
  @GetMapping
  public Iterable<Review> getReviews(@RequestParam(value = "productId", required = false) Optional<String> productId) {
    return productId.map(pid -> {
      return service.findByProductId(Integer.valueOf(pid))
          .map(Arrays::asList)
          .orElseGet(ArrayList::new);
    }).orElse(service.findAll());
  }

  /**
   * Creates a new review.
   * @param review    The review to create.
   * @return          The newly created review.
   */
  @PostMapping
  public ResponseEntity<Review> createReview(@RequestBody Review review) {
    log.info("Creating new review for product id: {}, {}", review.getProductId(), review);

    // Set the date for any entries in the review to now since we're creating the review now
    review.getEntries().forEach(entry -> entry.setDate(Instant.now()));

    // Save the review to the database
    Review newReview = service.save(review);
    log.info("Saved review: {}", newReview);

    try {
      // Build a created response
      return ResponseEntity
          .created(new URI(REQUEST_MAPPING + "/" + newReview.getId()))
          .eTag(Integer.toString(newReview.getVersion()))
          .body(newReview);
    } catch (URISyntaxException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Creates a new review entry for the review with the specified productId.
   * @param productId     The productId of the review to which to add the new review entry.
   * @param entry         The entry to add to the review.
   * @return              The complete updated review.
   */
  @PostMapping("/{productId}/entries")
  public ResponseEntity<Review> addEntryToReview(@PathVariable Integer productId, @RequestBody ReviewEntry entry) {
    log.info("Add review entry for product id: {}, {}", productId, entry);

    // Retrieve the review for the specified productId; if there is no review, create a new one
    Review review = service.findByProductId(productId).orElseGet(() -> new Review(productId));

    // Add this new entry to the review
    entry.setDate(Instant.now());
    review.getEntries().add(entry);

    // Save the review
    Review updatedReview = service.save(review);
    log.info("Updated review: {}", updatedReview);

    try {
      // Build a created response
      return ResponseEntity
          .ok()
          .location(new URI(REQUEST_MAPPING +"/"+ updatedReview.getId()))
          .eTag(Integer.toString(updatedReview.getVersion()))
          .body(updatedReview);
    } catch (URISyntaxException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  /**
   * Deletes the review with the specified ID. Note that this is the review ID, not the product ID.
   * @param id    The ID of the review to delete.
   * @return      A 200 OK on success, a 404 Not Found if the review does not exist.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteReview(@PathVariable String id) {

    log.info("Deleting review with ID {}", id);

    // Get the existing product
    Optional<Review> existingReview = service.findById(id);

    // Delete the review if it exists in the database
    return existingReview.map(review -> {
      service.delete(review.getId());
      return ResponseEntity.ok().build();
    }).orElse(ResponseEntity.notFound().build());
  }

}
