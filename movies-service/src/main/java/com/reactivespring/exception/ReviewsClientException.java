package com.reactivespring.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.ClientResponse;

public class ReviewsClientException extends RuntimeException {

  @Getter
  private final ClientResponse clientResponse;

  public ReviewsClientException(String message) {
    super(message);
    clientResponse = null;
  }

  public ReviewsClientException(ClientResponse clientResponse, String message) {
    super(message);
    this.clientResponse = clientResponse;
  }
}