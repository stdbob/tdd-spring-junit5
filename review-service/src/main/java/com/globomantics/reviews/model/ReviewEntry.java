package com.globomantics.reviews.model;

import java.time.Instant;

/**
 * A review entry. An entry is a user's review of a product and is contained in a Review document.
 */
public class ReviewEntry {
  /**
   * The username of the reviewer.
   */
  private String username;

  /**
   * The date that the review was written.
   */
  private Instant date;

  /**
   * The textual review content.
   */
  private String review;

  public ReviewEntry() {
  }

  public ReviewEntry(String username, Instant date, String review) {
    this.username = username;
    this.review = review;
    this.date = date;
  }

  public ReviewEntry(String username, String review) {
    this.username = username;
    this.review = review;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Instant getDate() {
    return date;
  }

  public void setDate(Instant date) {
    this.date = date;
  }

  public String getReview() {
    return review;
  }

  public void setReview(String review) {
    this.review = review;
  }

  @Override
  public String toString() {
    return "ReviewEntry{" +
        "username='" + username + '\'' +
        ", date=" + date +
        ", review='" + review + '\'' +
        '}';
  }
}
