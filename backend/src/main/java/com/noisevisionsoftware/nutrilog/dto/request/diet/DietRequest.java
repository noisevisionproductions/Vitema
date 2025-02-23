package com.noisevisionsoftware.nutrilog.dto.request.diet;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DietRequest {

    @NotBlank
    private String userId;

    @Valid
    @NotNull
    private List<DayRequest> days;

    @Valid
    private DietMetadataRequest metadata;
}