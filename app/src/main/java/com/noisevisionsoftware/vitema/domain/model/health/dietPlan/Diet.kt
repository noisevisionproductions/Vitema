package com.noisevisionsoftware.vitema.domain.model.health.dietPlan

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.JsonAdapter
import com.noisevisionsoftware.vitema.utils.TimestampAdapter

data class Diet(
    val id: String = "",
    val userId: String = "",

    @JsonAdapter(TimestampAdapter::class)
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @JsonAdapter(TimestampAdapter::class)
    @PropertyName("updatedAt")
    val updatedAt: Timestamp = Timestamp.now(),

    val days: List<DietDay> = emptyList(),
    val metadata: DietMetadata = DietMetadata()
)