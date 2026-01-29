package com.noisevisionsoftware.vitema.domain.exceptions

import android.util.Log
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementsInputState
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementsValidationState
import com.noisevisionsoftware.vitema.utils.DateUtils
import java.util.Calendar

object MeasurementValidation {
    fun validateAndUpdateMeasurements(
        measurements: MeasurementsInputState,
        onMeasurementsUpdate: (MeasurementsInputState) -> Unit,
        onConfirm: (BodyMeasurements) -> Unit
    ) {
        val bodyMeasurements = measurements.toBodyMeasurements()
        val validationResults = validateMeasurements(bodyMeasurements)

        val newValidationState = MeasurementsValidationState(
            weight = validationResults.find { it.field == "weight" }?.message,
            height = validationResults.find { it.field == "height" }?.message,
            neck = validationResults.find { it.field == "neck" }?.message,
            biceps = validationResults.find { it.field == "biceps" }?.message,
            chest = validationResults.find { it.field == "chest" }?.message,
            waist = validationResults.find { it.field == "waist" }?.message,
            hips = validationResults.find { it.field == "hips" }?.message,
            belt = validationResults.find { it.field == "belt" }?.message,
            thigh = validationResults.find { it.field == "thigh" }?.message,
            calf = validationResults.find { it.field == "calf" }?.message
        )

        onMeasurementsUpdate(measurements.copy(validationState = newValidationState))

        if (validationResults.isEmpty()) {
            Log.d("MeasurementValidation", "Validation successful, calling onConfirm")
            onConfirm(
                bodyMeasurements.copy(
                    measurementType = MeasurementType.FULL_BODY,
                    date = DateUtils.getCurrentLocalDate(),
                    weekNumber = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
                )
            )
        } else {
            Log.d("MeasurementValidation", "Validation failed: $validationResults")
        }
    }


    private fun validateMeasurements(measurements: BodyMeasurements): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (measurements.weight <= 40 || measurements.weight >= 250) {
            errors.add(ValidationError("weight", "Wprowadź prawidłową wagę (40-250 kg)"))
        }

        if (measurements.height <= 100 || measurements.height >= 220) {
            errors.add(ValidationError("height", "Wprowadź prawidłowy wzrost (100-220 cm)"))
        }

        validateMeasurement(
            measurements.neck,
            25..50,
            "neck",
            "Wprowadź prawidłowy obwód szyi (25-70 cm)",
            errors
        )
        validateMeasurement(
            measurements.biceps,
            20..70,
            "biceps",
            "Wprowadź prawidłowy obwód bicepsa (20-70 cm)",
            errors
        )
        validateMeasurement(
            measurements.chest,
            60..200,
            "chest",
            "Wprowadź prawidłowy obwód klatki piersiowej (60-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.belt,
            40..200,
            "belt",
            "Wprowadź prawidłowy obwód pasa (40-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.waist,
            40..200,
            "waist",
            "Wprowadź prawidłowy obwód talii (40-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.hips,
            40..200,
            "hips",
            "Wprowadź prawidłowy obwód bioder (40-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.thigh,
            20..100,
            "thigh",
            "Wprowadź prawidłowy obwód uda (20-100 cm)",
            errors
        )
        validateMeasurement(
            measurements.calf,
            20..70,
            "calf",
            "Wprowadź prawidłowy obwód łydki (20-70 cm)",
            errors
        )

        return errors
    }

    private fun validateMeasurement(
        value: Int,
        range: IntRange,
        field: String,
        errorMessage: String,
        errors: MutableList<ValidationError>
    ) {
        if (value <= 0 || value !in range) {
            errors.add(ValidationError(field, errorMessage))
        }
    }

    private fun MeasurementsInputState.toBodyMeasurements() = BodyMeasurements(
        neck = neck.toIntOrNull() ?: 0,
        biceps = biceps.toIntOrNull() ?: 0,
        chest = chest.toIntOrNull() ?: 0,
        waist = waist.toIntOrNull() ?: 0,
        belt = belt.toIntOrNull() ?: 0,
        hips = hips.toIntOrNull() ?: 0,
        thigh = thigh.toIntOrNull() ?: 0,
        calf = calf.toIntOrNull() ?: 0,
        weight = weight.toIntOrNull() ?: 0,
        height = height.toIntOrNull() ?: 0,
        note = note
    )

    data class ValidationError(
        val field: String,
        val message: String
    )

    fun validateCurrentPage(
        measurements: MeasurementsInputState,
        currentPage: Int,
        onMeasurementsUpdate: (MeasurementsInputState) -> Unit,
        onNextPage: () -> Unit
    ) {
        val bodyMeasurements = measurements.toBodyMeasurements()

        val validationResults = when (currentPage) {
            0 -> validateBasicMeasurements(bodyMeasurements)
            1 -> validateUpperBodyMeasurements(bodyMeasurements)
            2 -> validateLowerBodyMeasurements(bodyMeasurements)
            else -> emptyList()
        }

        val newValidationState = MeasurementsValidationState(
            // aktualizujemy tylko pola dla bieżącej strony
            weight = if (currentPage == 0) validationResults.find { it.field == "weight" }?.message else measurements.validationState.weight,
            height = if (currentPage == 0) validationResults.find { it.field == "height" }?.message else measurements.validationState.height,
            neck = if (currentPage == 1) validationResults.find { it.field == "neck" }?.message else measurements.validationState.neck,
            biceps = if (currentPage == 1) validationResults.find { it.field == "biceps" }?.message else measurements.validationState.biceps,
            chest = if (currentPage == 1) validationResults.find { it.field == "chest" }?.message else measurements.validationState.chest,
            waist = if (currentPage == 2) validationResults.find { it.field == "waist" }?.message else measurements.validationState.waist,
            hips = if (currentPage == 2) validationResults.find { it.field == "hips" }?.message else measurements.validationState.hips,
            thigh = if (currentPage == 2) validationResults.find { it.field == "thigh" }?.message else measurements.validationState.thigh,
            calf = if (currentPage == 2) validationResults.find { it.field == "calf" }?.message else measurements.validationState.calf
        )

        onMeasurementsUpdate(measurements.copy(validationState = newValidationState))

        if (validationResults.isEmpty()) {
            onNextPage()
        }
    }

    private fun validateBasicMeasurements(measurements: BodyMeasurements): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        if (measurements.weight <= 40 || measurements.weight >= 250) {
            errors.add(ValidationError("weight", "Wprowadź prawidłową wagę (40-250 kg)"))
        }
        if (measurements.height <= 100 || measurements.height >= 250) {
            errors.add(ValidationError("height", "Wprowadź prawidłowy wzrost (100-250 cm)"))
        }

        return errors
    }

    private fun validateUpperBodyMeasurements(measurements: BodyMeasurements): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        validateMeasurement(
            measurements.neck,
            20..70,
            "neck",
            "Wprowadź prawidłowy obwód szyi (20-70 cm)",
            errors
        )
        validateMeasurement(
            measurements.biceps,
            20..70,
            "biceps",
            "Wprowadź prawidłowy obwód bicepsa (20-70 cm)",
            errors
        )
        validateMeasurement(
            measurements.chest,
            60..200,
            "chest",
            "Wprowadź prawidłowy obwód klatki piersiowej (60-200 cm)",
            errors
        )

        return errors
    }

    private fun validateLowerBodyMeasurements(measurements: BodyMeasurements): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()

        validateMeasurement(
            measurements.waist,
            40..200,
            "waist",
            "Wprowadź prawidłowy obwód talii (40-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.hips,
            40..200,
            "hips",
            "Wprowadź prawidłowy obwód bioder (40-200 cm)",
            errors
        )
        validateMeasurement(
            measurements.thigh,
            20..100,
            "thigh",
            "Wprowadź prawidłowy obwód uda (20-100 cm)",
            errors
        )
        validateMeasurement(
            measurements.calf,
            20..70,
            "calf",
            "Wprowadź prawidłowy obwód łydki (20-70 cm)",
            errors
        )

        return errors
    }
}