package com.reactivespring.domain;


import java.time.LocalDate;
import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;

@Validated
public record MovieInfo(
    String id,

    @NotBlank(message = "info.name must be present")
    String name,

    @NotNull(message = "info.year should not be null")
    @Min(value = 1800, message = "info.year must be a valid year above 1800")
    @Max(value = 2200, message = "info.year can't be to far away in he future, max 2200.")
    Integer year,

    @NotNull(message = "info.cast must not be null")
    @NotEmpty(message = "info.cast must not be empty")
    List<String> cast,

    LocalDate releaseDate
) {

}