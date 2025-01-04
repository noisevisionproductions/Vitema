package com.noisevisionsoftware.szytadieta.domain.model.dietPlan.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.MealType
import java.lang.reflect.Type

class MealTypeAdapter : JsonSerializer<MealType>, JsonDeserializer<MealType> {
    override fun serialize(
        src: MealType,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(src.name)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MealType {
        return try {
            MealType.valueOf(json.asString)
        } catch (e: Exception) {
            MealType.BREAKFAST
        }
    }
}