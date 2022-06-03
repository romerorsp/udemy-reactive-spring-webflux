package com.reactivespring.dto;

import javax.validation.constraints.NotNull;

public record ReviewRatingChangeDTO(@NotNull(message = "[rating] cannot be null") Double rating) {

}
