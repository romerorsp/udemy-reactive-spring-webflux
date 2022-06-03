package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.ErrorMessage;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 0)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
        "rest.client.base-urls.movies-info=http://127.0.0.1:${wiremock.server.port}/v1/movieinfos",
        "rest.client.base-urls.reviews=http://127.0.0.1:${wiremock.server.port}/v1/reviews"
    }
)
public class MoviesControllerITTest {

  @Autowired
  private WebTestClient webTestClient;

  @Test
  void testFetchMovieById() {
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withBodyFile("movieinfo.json")
        )
    );
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlPathEqualTo("/v1/reviews")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withBodyFile("reviews.json")
        )
    );

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .isOk()
        .returnResult(Movie.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            movie ->
                "Sul Più Bello".equals(movie.info().name()) &&
                    List.of("Molto Buono!", "Excellent Movie!")
                        .containsAll(movie.reviews().stream().map(Review::comment).toList())
        ).verifyComplete();
  }

  @Test
  void testFetchMovieByIdMovieInfoNotFound() {
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withStatus(HttpStatus.NOT_FOUND.value())
        )
    );

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .isNotFound()
        .returnResult(ErrorMessage.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            error -> error.getStatusCode() == HttpStatus.NOT_FOUND.value() &&
                error.getMessages().contains("Unable to find info with id [sul-piu-bello]")
        ).verifyComplete();

    WireMock.verify(
        1, WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")));
  }

  @Test
  void testFetchMovieByIdMovieInfoServerError() {
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
        )
    );

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .returnResult(ErrorMessage.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            error -> error.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value() &&
                error.getMessages()
                    .contains(
                        "Unable to find info with id [sul-piu-bello] [server_down: try again!]")
        ).verifyComplete();

    WireMock.verify(
        4, WireMock.getRequestedFor(WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")));
  }

  @Test
  void testFetchMovieByIdReviewNotFound() {
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withBodyFile("movieinfo.json")
        )
    );
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlPathEqualTo("/v1/reviews")
        ).willReturn(WireMock.aResponse().withStatus(HttpStatus.NOT_FOUND.value()))
    );

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .isOk()
        .returnResult(Movie.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            movie ->
                "Sul Più Bello".equals(movie.info().name()) && movie.reviews().isEmpty()
        ).verifyComplete();

    WireMock.verify(
        1,
        WireMock.getRequestedFor(
            WireMock.urlEqualTo("/v1/reviews?movieInfoId=sul-piu-bello")
        )
    );
  }


  @Test
  void testFetchMovieByIdReviewServerError() {
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlEqualTo("/v1/movieinfos/sul-piu-bello")
        ).willReturn(
            WireMock.aResponse()
                .withHeader(
                    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE
                ).withBodyFile("movieinfo.json")
        )
    );
    WireMock.stubFor(
        WireMock.get(
            WireMock.urlPathEqualTo("/v1/reviews")
        ).willReturn(WireMock.aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value()))
    );

    var movieFlux = webTestClient.get()
        .uri("/v1/movies/{id}", "sul-piu-bello")
        .exchange()
        .expectStatus()
        .is5xxServerError()
        .returnResult(ErrorMessage.class)
        .getResponseBody();

    StepVerifier.create(movieFlux)
        .expectNextMatches(
            error -> error.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value() &&
                error.getMessages()
                    .contains(
                        "Unable to find reviews for movie info id [sul-piu-bello] [server_down: try again!]")
        ).verifyComplete();

    WireMock.verify(
        4,
        WireMock.getRequestedFor(
            WireMock.urlEqualTo("/v1/reviews?movieInfoId=sul-piu-bello")
        )
    );
  }
}
