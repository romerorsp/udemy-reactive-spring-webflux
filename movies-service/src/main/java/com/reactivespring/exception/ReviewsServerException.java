package com.reactivespring.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.ClientResponse;

public class ReviewsServerException extends RuntimeException {

  @Getter
  private final ClientResponse clientResponse;

  public ReviewsServerException(String message) {
    super(message);
    clientResponse = null;
  }

  public ReviewsServerException(ClientResponse clientResponse, String message) {
    super(message);
    this.clientResponse = clientResponse;
  }
}