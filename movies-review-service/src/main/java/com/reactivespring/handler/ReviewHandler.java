package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.dto.ReviewRatingChangeDTO;
import com.reactivespring.exception.ErrorMessage;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.repository.ReviewReactiveRepository;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Component
@RequiredArgsConstructor
public class ReviewHandler {

  private final Validator validator;

  private final ReviewReactiveRepository repository;

  private final Many<Review> reviewsSink = Sinks.many().replay().latest();

  public Mono<ServerResponse> addReview(ServerRequest serverRequest) {
    return serverRequest.bodyToMono(Review.class)
        .doOnNext(this::validate)
        .flatMap(repository::save)
        .doOnNext(reviewsSink::tryEmitNext)
        .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);

    //Replaced this validation by the GlobalErrorHandler...
//        .onErrorResume(ReviewDataException.class,
//            exception -> ServerResponse.status(
//                HttpStatus.valueOf(exception.getError().getStatusCode())
//            ).bodyValue(exception.getError())
//        );
  }

  private void validate(Review review) {
    var constraintViolations = validator.validate(review);
    if (constraintViolations.size() > 0) {
      var error = constraintViolations.stream()
          .map(ConstraintViolation::getMessage)
          .sorted()
          .reduce(
              new ErrorMessage(HttpStatus.BAD_REQUEST.value()),
              (errorMessage, message) -> {
                errorMessage.addMessage(message);
                return errorMessage;
              },
              (errorMessage1, errorMessage2) -> {
                errorMessage1.addMessages(errorMessage2.getMessages());
                return errorMessage1;
              }
          );
      throw new ReviewDataException(error);
    }
  }

  public Mono<ServerResponse> getReviews(ServerRequest serverRequest) {
    var movieInfoId = serverRequest.queryParam("movieInfoId");
    return movieInfoId.map(
        id -> buildServerResponseOkWithBody(repository.findByMovieInfoId(movieInfoId.get()))
    ).orElseGet(() -> buildServerResponseOkWithBody(repository.findAll()));
  }

  private Mono<ServerResponse> buildServerResponseOkWithBody(Flux<Review> bodyFlux) {
    return ServerResponse.ok().body(bodyFlux, Review.class);
  }

  public Mono<ServerResponse> updateReview(ServerRequest serverRequest) {
    var reviewId = serverRequest.pathVariable("id");
    return repository.findById(reviewId)
        .flatMap(fetched ->
            serverRequest.bodyToMono(ReviewRatingChangeDTO.class)
                .map(dto -> new Review(
                        fetched.id(),
                        fetched.movieInfoId(),
                        fetched.comment(),
                        dto.rating()
                    )
                ).flatMap(repository::save)
                .flatMap(ServerResponse.ok()::bodyValue)
        ).switchIfEmpty(ServerResponse.notFound().build());
    //Alternative way of doing it through GlobalErrorHandler given by the teacher in the course. [I don't like it much]
//    .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for id %s".formatted(reviewId))));
  }

  public Mono<ServerResponse> deleteReview(ServerRequest serverRequest) {
    var reviewId = serverRequest.pathVariable("id");
    return repository.findById(reviewId)
        .flatMap(fetched -> repository.deleteById(fetched.id())
            .then(ServerResponse.noContent().build())
        ).switchIfEmpty(ServerResponse.notFound().build());
  }

  public Mono<ServerResponse> getSinkReviews(
      @SuppressWarnings("unused") ServerRequest serverRequest) {
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_NDJSON)
        .body(reviewsSink.asFlux(), Review.class);
  }
}
