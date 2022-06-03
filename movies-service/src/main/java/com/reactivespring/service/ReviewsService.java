package com.reactivespring.service;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.properties.RestClientProperties;
import com.reactivespring.util.RetryUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ReviewsService {

  private final WebClient webClient;

  private final RestClientProperties restClientProperties;

  public Flux<Review> findByMovieInfoId(String movieInfoId) {
    var uri = UriComponentsBuilder.fromUriString(restClientProperties.baseUrls().reviews())
        .queryParam("movieInfoId", movieInfoId)
        .buildAndExpand()
        .toUri();
    return webClient.get()
        .uri(uri)
        .accept(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError,
            clientResponse -> {
              if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                return Mono.empty();
              }
              return Mono.error(
                  new ReviewsClientException(
                      clientResponse,
                      "Unable to find reviews for movie info id [%s]".formatted(movieInfoId)
                  )
              );
            }
        ).onStatus(HttpStatus::is5xxServerError,
            clientResponse -> Mono.error(
                new ReviewsServerException(
                    clientResponse,
                    "Unable to find reviews for movie info id [%s] [server_down: try again!]"
                        .formatted(movieInfoId)
                )
            )
        ).bodyToFlux(Review.class)
        .retryWhen(RetryUtil.retrySpec(ReviewsServerException.class));
  }
}
