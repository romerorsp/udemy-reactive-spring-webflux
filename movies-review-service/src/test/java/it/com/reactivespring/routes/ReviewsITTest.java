package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.dto.ReviewRatingChangeDTO;
import com.reactivespring.repository.ReviewReactiveRepository;
import java.util.List;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReviewsITTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ReviewReactiveRepository reviewRepository;

  @BeforeEach
  void setup() {
    var reviews = List.of(
        new Review(null, "sul-piu-bello", "Nice movie!", 6.5),
        new Review(null, "dark-knight-rises", "I didn't like it!", 1.5),
        new Review(null, "dark-knight-rises", "Best movie!", 10.0),
        new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
    );
    reviewRepository.saveAll(reviews).blockLast();
  }

  @AfterEach
  void tearDown() {
    reviewRepository.deleteAll().block();
  }

  @Test
  void testAddReview() {
    var newReview = new Review(null, "dark-knight-rises", "Average.", 5.0);

    var addReviewFlux =
        webTestClient.post()
            .uri("/v1/reviews")
            .bodyValue(newReview)
            .exchange()
            .expectStatus()
            .isCreated()
            .returnResult(Review.class)
            .getResponseBody();

    StepVerifier.create(addReviewFlux)
        .expectNextMatches(
            review -> review != null &&
                review.id() != null &&
                newReview.movieInfoId().equals(review.movieInfoId()))
        .verifyComplete();
  }

  @Test
  void testAddInvalidReview() {
    var newReview = new Review(null, "", null, -1.0);

    webTestClient.post()
        .uri("/v1/reviews")
        .bodyValue(newReview)
        .exchange()
        .expectStatus()
        .isBadRequest()
        .expectBody()
        .jsonPath("$.messages")
        .<JSONArray>value(
            value -> Assertions.assertTrue(
                value.containsAll(
                    List.of(
                        "review.comment must not be empty.",
                        "review.movieInfoId must not be empty.",
                        "review.rating must be a value between 0 and 10"
                    )
                )
            )
        );
  }

  @Test
  void testGetAllReviews() {
    var getAllReviewsFlux =
        webTestClient.get()
            .uri("/v1/reviews")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Review.class)
            .getResponseBody();

    StepVerifier.create(getAllReviewsFlux)
        .expectNextCount(4)
        .verifyComplete();
  }

  @Test
  void testUpdateReview() {
    var updateReviewFlux =
        webTestClient.put()
            .uri("/v1/reviews/{id}", "regular-review")
            .bodyValue(new ReviewRatingChangeDTO(5.5))
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Review.class)
            .getResponseBody();

    StepVerifier.create(updateReviewFlux)
        .expectNextMatches(review -> review.rating() == 5.5)
        .verifyComplete();
  }

  @Test
  void testUpdateReviewNotFound() {
    webTestClient.put()
        .uri("/v1/reviews/{id}", "regular-review-INVALID")
        .bodyValue(new ReviewRatingChangeDTO(5.5))
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .isEmpty();
  }

  @Test
  void testDeleteReview() {
    webTestClient.delete()
        .uri("/v1/reviews/{id}", "regular-review")
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody()
        .isEmpty();
  }

  @Test
  void testDeleteReviewNotFound() {
    webTestClient.delete()
        .uri("/v1/reviews/{id}", "regular-review-INVALID")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .isEmpty();
  }

  @Test
  void testGetReviewsByMovieInfoId() {
    var uri = UriComponentsBuilder.fromUriString("/v1/reviews")
        .queryParam("movieInfoId", "dark-knight-rises")
        .buildAndExpand()
        .toUri();
    var resultFlux = webTestClient.get()
        .uri(uri)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Review.class)
        .getResponseBody();

    StepVerifier.create(resultFlux)
        .expectNextCount(3)
        .verifyComplete();
  }

  @Test
  void testGetAllSinkReviews() {

    testAddReview();

    var getAllSinkReviewsFlux = webTestClient.get()
        .uri("/v1/reviews/stream")
        .accept(MediaType.APPLICATION_NDJSON)
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Review.class)
        .getResponseBody();

    StepVerifier.create(getAllSinkReviewsFlux)
        .expectNextMatches(review -> review.id() != null)
        .thenCancel()
        .verify();
  }
}