package com.reactivespring.exceptionhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

  private final ObjectMapper mapper;

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    var bufferFactory = exchange.getResponse().bufferFactory();

    if (ex instanceof ReviewDataException reviewDataException) {
      try {
        var errorMessage = bufferFactory.wrap(
            mapper.writeValueAsBytes(reviewDataException.getError())
        );
        exchange.getResponse().setRawStatusCode(reviewDataException.getError().getStatusCode());
        return exchange.getResponse().writeWith(Mono.just(errorMessage));
      } catch (JsonProcessingException e) {
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse()
            .writeWith(
                Mono.just(
                    bufferFactory.wrap(e.getMessage().getBytes(StandardCharsets.UTF_8))
                )
            );
      }
    }
    if (ex instanceof ReviewNotFoundException reviewNotFoundException) {
      exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
      return exchange.getResponse()
          .writeWith(
              Mono.just(
                  bufferFactory.wrap(
                      reviewNotFoundException.getMessage()
                          .getBytes(StandardCharsets.UTF_8)
                  )
              )
          );
    }

    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    return exchange.getResponse()
        .writeWith(
            Mono.just(
                bufferFactory.wrap(ex.getMessage().getBytes(StandardCharsets.UTF_8))
            )
        );
  }
}