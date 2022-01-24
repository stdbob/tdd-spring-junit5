package com.globomantics.reviews;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globomantics.reviews.model.Review;
import com.globomantics.reviews.model.ReviewEntry;
import com.globomantics.reviews.repository.MongoDataFile;
import com.globomantics.reviews.repository.MongoSpringExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({MongoSpringExtension.class})
public class ReviewServiceIntegrationTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MongoTemplate mongoTemplate;

  /**
   * MongoSpringExtension method that returns the autowired MongoTemplate to use for MongoDB interactions.
   *
   * @return The autowired MongoTemplate instance.
   */
  public MongoTemplate getMongoTemplate() {
    return mongoTemplate;
  }

  @Test
  @DisplayName("GET /reviews/1 - Found")
  @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
  void testGetReviewByIdFound() throws Exception {

    // Execute the GET request
    mockMvc.perform(get("/reviews/{id}", 1))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
        .andExpect(header().string(HttpHeaders.LOCATION, "/reviews/1"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is("1")))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.version", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(1)))
        .andExpect(jsonPath("$.entries[0].username", is("user1")))
        .andExpect(jsonPath("$.entries[0].review", is("This is a review")))
        .andExpect(jsonPath("$.entries[0].date", is("2018-11-10T11:38:26.855Z")));
  }

  @Test
  @DisplayName("GET /reviews/99 - Not Found")
  @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
  void testGetReviewByIdNotFound() throws Exception {

    // Execute the GET request
    mockMvc.perform(get("/reviews/{id}", 99))

        // Validate that we get a 404 Not Found response
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /reviews - Success")
  @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
  void testCreateReview() throws Exception {
    // Setup mocked service
    Review postReview = new Review(1);
    postReview.getEntries().add(new ReviewEntry("test-user", "Great product"));

    mockMvc.perform(post("/reviews")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(postReview)))

        // Validate the response code and content type
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
        .andExpect(header().exists(HttpHeaders.LOCATION))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", any(String.class)))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.version", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(1)))
        .andExpect(jsonPath("$.entries[0].username", is("test-user")))
        .andExpect(jsonPath("$.entries[0].review", is("Great product")))
        .andExpect(jsonPath("$.entries[0].date", any(String.class)));
  }

  @Test
  @DisplayName("POST /reviews/{productId}/entries")
  @MongoDataFile(value = "sample.json", classType = Review.class, collectionName = "Reviews")
  void testAddEntryToReview() throws Exception {
    // Setup mocked service
    ReviewEntry reviewEntry = new ReviewEntry("test-user", "Great product");

    mockMvc.perform(post("/reviews/{productId}/entries", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(reviewEntry)))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
        .andExpect(header().string(HttpHeaders.LOCATION, "/reviews/1"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is("1")))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(2)))
        .andExpect(jsonPath("$.entries[0].username", is("user1")))
        .andExpect(jsonPath("$.entries[0].review", is("This is a review")))
        .andExpect(jsonPath("$.entries[0].date", is("2018-11-10T11:38:26.855Z")))
        .andExpect(jsonPath("$.entries[1].username", is("test-user")))
        .andExpect(jsonPath("$.entries[1].review", is("Great product")))
        .andExpect(jsonPath("$.entries[1].date", any(String.class)));
  }

  static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
