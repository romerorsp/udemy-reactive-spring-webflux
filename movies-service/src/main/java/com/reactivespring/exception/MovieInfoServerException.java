package com.reactivespring.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.ClientResponse;

public class MovieInfoServerException extends RuntimeException {

  @Getter
  private final ClientResponse clientResponse;

  public MovieInfoServerException(String message) {
    super(message);
    clientResponse = null;
  }

  public MovieInfoServerException(ClientResponse clientResponse, String message) {
    super(message);
    this.clientResponse = clientResponse;
  }
}