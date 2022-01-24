package com.globomantics.reviews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.globomantics.reviews.model.Review;
import com.globomantics.reviews.model.ReviewEntry;
import com.globomantics.reviews.service.ReviewService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

  @MockBean
  private ReviewService service;

  @Autowired
  private MockMvc mockMvc;

  /**
   * Create a DateFormat that we can use to compare SpringMVC returned dates to expected values.
   */
  private static DateTimeFormatter df = DateTimeFormatter.ISO_DATE_TIME;

  @BeforeAll
  static void beforeAll() {
    // Spring's dates are configured to GMT, so adjust our timezone accordingly
//    df.(TimeZone.getTimeZone("GMT"));
  }

  @Test
  @DisplayName("GET /reviews/reviewId - Found")
  void testGetReviewByIdFound() throws Exception {
    // Setup our mocked service
    Review mockReview = new Review("reviewId", 1, 1);
    Instant now = Instant.now();
    mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));
    doReturn(Optional.of(mockReview)).when(service).findById("reviewId");

    // Execute the GET request
    mockMvc.perform(get(ReviewController.REQUEST_MAPPING + "/{id}", "reviewId"))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
        .andExpect(header().string(HttpHeaders.LOCATION, ReviewController.REQUEST_MAPPING + "/reviewId"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is("reviewId")))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(1)))
        .andExpect(jsonPath("$.entries[0].username", is("test-user")))
        .andExpect(jsonPath("$.entries[0].review", is("Great product")))
        .andExpect(jsonPath("$.entries[0].date", is(now.toString())));
  }

  @Test
  @DisplayName("GET /reviews/reviewId - Not Found")
  void testGetReviewByIdNotFound() throws Exception {
    // Setup our mocked service
    doReturn(Optional.empty()).when(service).findById("reviewId");

    // Execute the GET request
    mockMvc.perform(get(ReviewController.REQUEST_MAPPING + "/{id}", "reviewId"))

        // Validate that we get a 404 Not Found response
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST /reviews - Success")
  void testCreateReview() throws Exception {
    // Setup mocked service
    Instant now = Instant.now();
    Review postReview = new Review(1);
    postReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));

    Review mockReview = new Review("reviewId", 1, 1);
    mockReview.getEntries().add(new ReviewEntry("test-user", now, "Great product"));

    doReturn(mockReview).when(service).save(any());

    mockMvc.perform(post(ReviewController.REQUEST_MAPPING + "")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(mockReview)))

        // Validate the response code and content type
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))

        // Validate the headers
        .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))
        .andExpect(header().string(HttpHeaders.LOCATION, ReviewController.REQUEST_MAPPING + "/reviewId"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is("reviewId")))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(1)))
        .andExpect(jsonPath("$.entries[0].username", is("test-user")))
        .andExpect(jsonPath("$.entries[0].review", is("Great product")))
        .andExpect(jsonPath("$.entries[0].date", is(now.toString())));
  }

  @Test
  @DisplayName("POST /reviews/{productId}/entries")
  void testAddEntryToReview() throws Exception {
    // Setup mocked service
    Instant now = Instant.now();
    ReviewEntry reviewEntry = new ReviewEntry("test-user", now, "Great product");
    Review mockReview = new Review("1", 1, 1);
    Review returnedReview = new Review("1", 1, 2);
    returnedReview.getEntries().add(reviewEntry);

    // Handle lookup
    doReturn(Optional.of(mockReview)).when(service).findByProductId(1);

    // Handle save
    doReturn(returnedReview).when(service).save(any());

    mockMvc.perform(post(ReviewController.REQUEST_MAPPING + "/{productId}/entries", 1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(reviewEntry)))

        // Validate the response code and content type
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ETAG, "\"2\""))
        .andExpect(header().string(HttpHeaders.LOCATION, ReviewController.REQUEST_MAPPING + "/1"))

        // Validate the returned fields
        .andExpect(jsonPath("$.id", is("1")))
        .andExpect(jsonPath("$.productId", is(1)))
        .andExpect(jsonPath("$.entries.length()", is(1)))
        .andExpect(jsonPath("$.entries[0].username", is("test-user")))
        .andExpect(jsonPath("$.entries[0].review", is("Great product")));
  }


  static String asJsonString(final Object obj) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.registerModule(new JavaTimeModule());
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}