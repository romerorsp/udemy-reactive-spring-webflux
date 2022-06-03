package com.reactivespring.domain;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

@Document
@Validated
public record Review(
    @Id
    String id,

    @NotEmpty(message = "review.movieInfoId must not be empty.")
    String movieInfoId,

    @NotEmpty(message = "review.comment must not be empty.")
    String comment,

    @Min(value = 0L, message = "review.rating must be a value between 0 and 10")
    @Max(value = 10L, message = "review.rating must be a value between 0 and 10")
    Double rating
) {

}
