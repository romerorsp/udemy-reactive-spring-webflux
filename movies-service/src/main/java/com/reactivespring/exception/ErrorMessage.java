package com.reactivespring.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
@JsonRootName(value = "errors")
@JsonInclude(Include.NON_EMPTY)
public class ErrorMessage {

  private List<String> messages;

  private int statusCode;

  public ErrorMessage() {
    messages = new ArrayList<>();
    statusCode = 500;
  }

  public ErrorMessage(int statusCode) {
    messages = new ArrayList<>();
    this.statusCode = statusCode;
  }

  public ErrorMessage(List<String> messages, int statusCode) {
    if (messages == null) {
      this.messages = new ArrayList<>();
    } else {
      this.messages = messages;
    }

    if (statusCode < 1) {
      this.statusCode = 500;
    } else {
      this.statusCode = statusCode;
    }
  }

  public void addMessage(String message) {
    messages.add(message);
  }

  public void addMessages(List<String> messages) {
    messages.addAll(messages);
  }
}
