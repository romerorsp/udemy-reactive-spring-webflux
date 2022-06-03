package com.reactivespring.exception;

import lombok.Getter;

public class ReviewDataException extends RuntimeException {

  @Getter
  private final ErrorMessage error;

  public ReviewDataException(ErrorMessage error) {
    super("Validation Error!");
    this.error = error;
  }
}
