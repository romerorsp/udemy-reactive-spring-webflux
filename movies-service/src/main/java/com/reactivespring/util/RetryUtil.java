package com.reactivespring.util;

import java.time.Duration;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;

public class RetryUtil {


  public static Retry retrySpec(Class<?> clazz) {
    return Retry.fixedDelay(3, Duration.ofSeconds(1))
        .filter(clazz::isInstance)
        .onRetryExhaustedThrow(
            (retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure())
        );
  }
}
