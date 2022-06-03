package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import com.reactivespring.service.MoviesInfoService;
import com.reactivespring.service.ReviewsService;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@WebFluxTest(controllers = MoviesController.class)
public class MoviesControllerUnitTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private MoviesInfoService moviesInfoService;

  @MockBean
  private ReviewsService reviewsService;

  @Test
  void testFetchMovieById() {
    var sulPiuBelloMovieInfo = new MovieInfo(
        "sul-piu-bello",
        "Sul Più Bello",
        2020,
        List.of(
            "Ludovica Francesconi",
            "Giuseppe Maggio",
            "Gaja Masciale",
            "Jozef Gjura"),
        LocalDate.parse("2020-06-18")
    );
    var sulPiuBelloReviews = List.of(
        new Review("1", "sul-piu-bello", "Nice movie!", 6.5),
        new Review("2", "sul-piu-bello", "Capolavoro!", 10.),
        new Review("3", "sul-piu-bello", "Mi Piace!", 7.0)
    );
    Mockito.when(moviesInfoService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Mono.just(sulPiuBelloMovieInfo));
    Mockito.when(reviewsService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Flux.fromIterable(sulPiuBelloReviews));

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Movie.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            movie ->
                "Sul Più Bello".equals(movie.info().name()) &&
                    "1,2,3".equals(
                        movie.reviews()
                            .stream()
                            .map(Review::id)
                            .collect(Collectors.joining(","))
                    ))
        .verifyComplete();
    Mockito.verify(moviesInfoService).findByMovieInfoId(Mockito.anyString());
    Mockito.verify(reviewsService).findByMovieInfoId(Mockito.anyString());
  }

  @Test
  void testFetchMovieByIdWhenMovieInfoIsEmpty() {
    var sulPiuBelloReviews = List.of(
        new Review("1", "sul-piu-bello", "Nice movie!", 6.5),
        new Review("2", "sul-piu-bello", "Capolavoro!", 10.),
        new Review("3", "sul-piu-bello", "Mi Piace!", 7.0)
    );
    Mockito.when(moviesInfoService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Mono.empty());
    Mockito.when(reviewsService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Flux.fromIterable(sulPiuBelloReviews));

    webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .isEmpty();

    Mockito.verify(moviesInfoService).findByMovieInfoId(Mockito.anyString());
    Mockito.verify(reviewsService, Mockito.never()).findByMovieInfoId(Mockito.anyString());
  }

  @Test
  void testFetchMovieByIdWhenReviewsAreEmpty() {
    var sulPiuBelloMovieInfo = new MovieInfo(
        "sul-piu-bello",
        "Sul Più Bello",
        2020,
        List.of(
            "Ludovica Francesconi",
            "Giuseppe Maggio",
            "Gaja Masciale",
            "Jozef Gjura"),
        LocalDate.parse("2020-06-18")
    );
    Mockito.when(moviesInfoService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Mono.just(sulPiuBelloMovieInfo));
    Mockito.when(reviewsService.findByMovieInfoId(Mockito.eq("sul-piu-bello")))
        .thenReturn(Flux.empty());

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .is2xxSuccessful()
        .returnResult(Movie.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            movie ->
                "Sul Più Bello".equals(movie.info().name()) && movie.reviews().isEmpty()
        ).verifyComplete();

    Mockito.verify(moviesInfoService).findByMovieInfoId(Mockito.anyString());
    Mockito.verify(reviewsService).findByMovieInfoId(Mockito.anyString());
  }
}
