package com.reactivespring.domain;


import java.util.List;

public record Movie(
    MovieInfo info,
    List<Review> reviews
) {

}
