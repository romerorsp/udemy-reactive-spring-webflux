package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.dto.ReviewRatingChangeDTO;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import java.util.List;
import java.util.UUID;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private ReviewReactiveRepository repository;

  @Test
  void testAddReview() {
    var newReview = new Review(null, "dark-knight-rises", "Average.", 5.0);

    Mockito.when(repository.save(Mockito.eq(newReview)))
        .thenReturn(Mono.just(
            new Review(
                UUID.randomUUID().toString(),
                newReview.movieInfoId(),
                newReview.comment(),
                newReview.rating()
            )));

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
    Mockito.verify(repository).save(Mockito.eq(newReview));
  }


  @Test
  void testAddInvalidReview() {
    var newReview = new Review(null, "", null, -1.0);

    Mockito.when(repository.save(Mockito.eq(newReview)))
        .thenReturn(Mono.just(
            new Review(
                UUID.randomUUID().toString(),
                newReview.movieInfoId(),
                newReview.comment(),
                newReview.rating()
            )));

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

    Mockito.verify(repository, Mockito.never()).save(Mockito.any());
  }

  @Test
  void testGetAllReviews() {
    var reviews = List.of(
        new Review(null, "sul-piu-bello", "Nice movie!", 6.5),
        new Review(null, "dark-knight-rises", "I didn't like it!", 1.5),
        new Review(null, "dark-knight-rises", "Best movie!", 10.0),
        new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
    );
    Mockito.when(repository.findAll()).thenReturn(Flux.fromIterable(reviews));

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
    Mockito.verify(repository).findAll();
  }

  @Test
  void testUpdateReview() {
    Mockito.when(repository.findById(Mockito.eq("regular-review")))
        .thenReturn(
            Mono.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
            )
        );
    Mockito.when(repository.save(Mockito.any()))
        .thenReturn(
            Mono.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 5.5)
            )
        );

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

    Mockito.verify(repository).findById(Mockito.eq("regular-review"));
    Mockito.verify(repository).save(Mockito.any());
  }

  @Test
  void testUpdateReviewNotFound() {
    Mockito.when(repository.findById(Mockito.eq("regular-review")))
        .thenReturn(
            Mono.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
            )
        );
    Mockito.when(repository.findById(Mockito.eq("regular-review-INVALID")))
        .thenReturn(Mono.empty());

    webTestClient.put()
        .uri("/v1/reviews/{id}", "regular-review-INVALID")
        .bodyValue(new ReviewRatingChangeDTO(5.5))
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .isEmpty();
    Mockito.verify(repository, Mockito.never()).findById(Mockito.eq("regular-review"));
    Mockito.verify(repository).findById(Mockito.eq("regular-review-INVALID"));
  }

  @Test
  void testDeleteReview() {
    Mockito.when(repository.findById(Mockito.eq("regular-review")))
        .thenReturn(
            Mono.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
            )
        );
    Mockito.when(repository.deleteById(Mockito.eq("regular-review")))
        .thenReturn(Mono.empty());

    webTestClient.delete()
        .uri("/v1/reviews/{id}", "regular-review")
        .exchange()
        .expectStatus()
        .isNoContent()
        .expectBody()
        .isEmpty();

    Mockito.verify(repository).findById("regular-review");
    Mockito.verify(repository).deleteById(Mockito.eq("regular-review"));
  }

  @Test
  void testDeleteReviewNotFound() {
    Mockito.when(repository.findById(Mockito.eq("regular-review")))
        .thenReturn(
            Mono.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
            )
        );
    Mockito.when(repository.findById(Mockito.eq("regular-review-INVALID")))
        .thenReturn(Mono.empty());

    Mockito.when(repository.deleteById(Mockito.eq("regular-review")))
        .thenReturn(Mono.empty());
    Mockito.when(repository.deleteById(Mockito.eq("regular-review-INVALID")))
        .thenReturn(Mono.empty());

    webTestClient.delete()
        .uri("/v1/reviews/{id}", "regular-review-INVALID")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .isEmpty();

    Mockito.verify(repository).findById("regular-review-INVALID");
    Mockito.verify(repository, Mockito.never()).deleteById(Mockito.eq("regular-review-INVALID"));
  }

  @Test
  void testGetReviewsByMovieInfoId() {
    Mockito.when(repository.findByMovieInfoId(Mockito.eq("dark-knight-rises")))
        .thenReturn(
            Flux.just(
                new Review("regular-review", "dark-knight-rises", "Regular!", 6.5)
            )
        );

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
        .expectNextCount(1)
        .verifyComplete();

    Mockito.verify(repository).findByMovieInfoId(Mockito.eq("dark-knight-rises"));
  }
}