package com.noisevisionsoftware.nutrilog.dto.response.shopping;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListResponse {
    private String id;
    private String dietId;
    private String userId;
    private Map<String, List<CategorizedShoppingListItemResponse>> items;
    private LocalDateTime createdAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int version;
}
