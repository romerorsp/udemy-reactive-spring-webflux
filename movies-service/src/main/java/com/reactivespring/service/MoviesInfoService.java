package com.reactivespring.service;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MovieInfoClientException;
import com.reactivespring.exception.MovieInfoServerException;
import com.reactivespring.properties.RestClientProperties;
import com.reactivespring.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MoviesInfoService {

  private final WebClient webClient;

  private final RestClientProperties restClientProperties;

  public Mono<MovieInfo> findByMovieInfoId(String movieInfoId) {
    return webClient.get()
        .uri("%s/{id}".formatted(restClientProperties.baseUrls().moviesInfo()), movieInfoId)
        .accept(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError,
            clientResponse -> Mono.error(
                new MovieInfoClientException(
                    clientResponse,
                    "Unable to find info with id [%s]".formatted(movieInfoId)
                )
            )
        ).onStatus(HttpStatus::is5xxServerError,
            clientResponse -> Mono.error(
                new MovieInfoServerException(
                    clientResponse,
                    "Unable to find info with id [%s] [server_down: try again!]"
                        .formatted(movieInfoId)
                )
            )
        ).bodyToMono(MovieInfo.class)
//        .retry(3);
        .retryWhen(RetryUtil.retrySpec(MovieInfoServerException.class));
  }

  public Flux<MovieInfo> getAllMoviesInfoStream() {
    return webClient.get()
        .uri("%s/stream".formatted(restClientProperties.baseUrls().moviesInfo()))
        .accept(MediaType.APPLICATION_NDJSON)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError,
            clientResponse -> Mono.error(
                new MovieInfoClientException(clientResponse, "Unable to find any movie info.")
            )
        ).onStatus(HttpStatus::is5xxServerError,
            clientResponse -> Mono.error(
                new MovieInfoServerException(clientResponse, "Unable to find ay movie info.")
            )
        ).bodyToFlux(MovieInfo.class)
        .retryWhen(RetryUtil.retrySpec(MovieInfoServerException.class));
  }
}