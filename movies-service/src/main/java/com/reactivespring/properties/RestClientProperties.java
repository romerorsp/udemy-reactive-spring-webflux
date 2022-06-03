package com.reactivespring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rest.client")
public record RestClientProperties(
    BaseUrls baseUrls
) {


  public record BaseUrls(
      String moviesInfo,
      String reviews
  ) {

  }
}