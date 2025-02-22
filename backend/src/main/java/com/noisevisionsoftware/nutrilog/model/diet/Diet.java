package com.noisevisionsoftware.nutrilog.model.diet;

import com.google.cloud.Timestamp;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Setter
@Builder
public class Diet {
    private String id;
    private String userId;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<Day> days;
    private DietMetadata metadata;
}
