package com.noisevisionsoftware.nutrilog.dto.request.diet;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DayRequest {
    @NotBlank
    private String date;

    @Valid
    @NotNull
    private List<DayMealRequest> meals;
}