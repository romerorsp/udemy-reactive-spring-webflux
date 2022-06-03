package com.reactivespring.exception;

import lombok.Getter;
import org.springframework.web.reactive.function.client.ClientResponse;

public class MovieInfoClientException extends RuntimeException {

  @Getter
  private final ClientResponse clientResponse;

  public MovieInfoClientException(String message) {
    super(message);
    clientResponse = null;
  }

  public MovieInfoClientException(ClientResponse clientResponse, String message) {
    super(message);
    this.clientResponse = clientResponse;
  }
}