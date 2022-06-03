package com.reactivespring.errorhandler;


import com.reactivespring.exception.ErrorMessage;
import com.reactivespring.exception.MovieInfoClientException;
import com.reactivespring.exception.MovieInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalErrorHandler {

  @ExceptionHandler(MovieInfoClientException.class)
  public ResponseEntity<ErrorMessage> handleMovieInfoClientException(
      MovieInfoClientException e) {
    var errorMessage = new ErrorMessage(
        List.of(e.getMessage()),
        e.getClientResponse().rawStatusCode()
    );
    return ResponseEntity.status(e.getClientResponse().rawStatusCode()).body(errorMessage);
  }

  @ExceptionHandler(MovieInfoServerException.class)
  public ResponseEntity<ErrorMessage> handleMovieInfoServerException(
      MovieInfoServerException e) {
    var errorMessage = new ErrorMessage(
        List.of(e.getMessage()),
        e.getClientResponse().rawStatusCode()
    );
    return ResponseEntity.status(e.getClientResponse().rawStatusCode()).body(errorMessage);
  }

  @ExceptionHandler(ReviewsClientException.class)
  public ResponseEntity<ErrorMessage> handleReviewsClientException(
      ReviewsClientException e) {
    var errorMessage = new ErrorMessage(
        List.of(e.getMessage()),
        e.getClientResponse().rawStatusCode()
    );
    return ResponseEntity.status(e.getClientResponse().rawStatusCode()).body(errorMessage);
  }

  @ExceptionHandler(ReviewsServerException.class)
  public ResponseEntity<ErrorMessage> handleReviewsServerException(
      ReviewsServerException e) {
    var errorMessage = new ErrorMessage(
        List.of(e.getMessage()),
        e.getClientResponse().rawStatusCode()
    );
    return ResponseEntity.status(e.getClientResponse().rawStatusCode()).body(errorMessage);
  }
}