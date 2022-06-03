package com.reactivespring.controller;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import com.reactivespring.service.MoviesInfoService;
import com.reactivespring.service.ReviewsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/movies")
public class MoviesController {

  private final MoviesInfoService moviesInfoService;

  private final ReviewsService reviewsService;

  @GetMapping("/{id}")
  public Mono<ResponseEntity<Movie>> fetchMovieById(
      @PathVariable String id
  ) {
    return fetchMovieInfoById(id)
        .flatMap(movieInfo ->
            Mono.zip(Mono.justOrEmpty(movieInfo), fetchReviewsByMovieInfoId(id), Movie::new)
        ).map(ResponseEntity::ok)
        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
  public Flux<MovieInfo> fetchMovieInfoStream() {
    return moviesInfoService.getAllMoviesInfoStream();
  }

  private Mono<List<Review>> fetchReviewsByMovieInfoId(String id) {
    return Mono.just(id)
        .flatMap(movieInfoId -> reviewsService.findByMovieInfoId(movieInfoId).collectList());
  }

  private Mono<MovieInfo> fetchMovieInfoById(String id) {
    return Mono.just(id)
        .flatMap(moviesInfoService::findByMovieInfoId);
  }

}
