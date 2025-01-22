package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.exceptions.MeasurementValidation
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.MeasurementsInputState
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.BodyMeasurementsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementsDialog(
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurements) -> Unit,
    viewModel: BodyMeasurementsViewModel
) {
    var measurements by remember { mutableStateOf(MeasurementsInputState()) }
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

    val hasBasicErrors = measurements.run {
        validationState.weight != null || validationState.height != null
    }

    val hasUpperBodyErrors = measurements.run {
        validationState.neck != null || validationState.biceps != null || validationState.chest != null ||
                validationState.belt != null
    }
    val hasLowerBodyErrors = measurements.run {
        validationState.waist != null || validationState.hips != null ||
                validationState.thigh != null || validationState.calf != null
    }

    BasicAlertDialog(
        onDismissRequest = {}
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj nowe pomiary",
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = {
                            scope.launch {
                                viewModel.getLastMeasurements()?.let { lastMeasurements ->
                                    measurements = lastMeasurements.toInputState()
                                }
                            }
                        })
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.getLastMeasurements()?.let { lastMeasurements ->
                                    measurements = lastMeasurements.toInputState()
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Wypełnij ostatnimi pomiarami",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Wypełnij ostatnimi pomiarami",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                PageIndicators(
                    pagerState = pagerState,
                    hasBasicErrors = hasBasicErrors,
                    hasUpperBodyErrors = hasUpperBodyErrors,
                    hasLowerBodyErrors = hasLowerBodyErrors,
                    scope = scope,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f),
                ) { page ->
                    when (page) {
                        0 -> BasicMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 0
                        )

                        1 -> UpperBodyMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 1
                        )

                        2 -> LowerBodyMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 2
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Anuluj")
                    }

                    Row {
                        if (pagerState.currentPage > 0) {
                            TextButton(onClick = {
                                scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                            }) {
                                Text("Wstecz")
                            }
                        }

                        if (pagerState.currentPage < 2) {
                            TextButton(
                                onClick = {
                                    MeasurementValidation.validateCurrentPage(
                                        measurements = measurements,
                                        currentPage = pagerState.currentPage,
                                        onMeasurementsUpdate = { updatedMeasurements ->
                                            measurements = updatedMeasurements
                                        },
                                        onNextPage = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    )
                                }
                            ) {
                                Text("Dalej")
                            }
                        } else {
                            TextButton(
                                onClick = {
                                    MeasurementValidation.validateAndUpdateMeasurements(
                                        measurements = measurements,
                                        onMeasurementsUpdate = { updatedMeasurements ->
                                            measurements = updatedMeasurements
                                        },
                                        onConfirm = { validMeasurements ->
                                            onConfirm(validMeasurements)
                                        }
                                    )
                                }
                            ) {
                                Text("Dodaj")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PageIndicators(
    pagerState: PagerState,
    hasBasicErrors: Boolean,
    hasUpperBodyErrors: Boolean,
    hasLowerBodyErrors: Boolean,
    scope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.wrapContentWidth()
        ) {
            PageIndicator(
                isSelected = pagerState.currentPage == 0,
                hasError = hasBasicErrors,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
            )
            PageIndicator(
                isSelected = pagerState.currentPage == 1,
                hasError = hasUpperBodyErrors,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
            )
            PageIndicator(
                isSelected = pagerState.currentPage == 2,
                hasError = hasLowerBodyErrors,
                onClick = { scope.launch { pagerState.animateScrollToPage(2) } }
            )
        }
    }
}

@Composable
private fun PageIndicator(
    isSelected: Boolean,
    hasError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(
        targetState = hasError,
        label = "Error Animation"
    )

    val scale by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            } else {
                tween(durationMillis = 400)
            }
        },
        label = "Scale Animation"
    ) { error ->
        if (error) 1.2f else 1f
    }
    Box(
        modifier = modifier
            .size(16.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                when {
                    hasError -> MaterialTheme.colorScheme.error
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(onClick = onClick)
    )
}

@Composable
private fun BasicMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val weightFieldFocusRequester = remember { FocusRequester() }
    val heightFieldFocusRequester = remember { FocusRequester() }
    val noteFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            weightFieldFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Podstawowe pomiary",
            style = MaterialTheme.typography.titleMedium
        )

        MeasurementField(
            label = "Waga",
            value = TextFieldValue(measurements.weight, selection = TextRange(measurements.weight.length)),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(weight = newValue.text)) },
            unit = "kg",
            errorMessage = measurements.validationState.weight,
            imeAction = ImeAction.Next,
            focusRequester = weightFieldFocusRequester,
            keyboardActions = KeyboardActions(
                onNext = {
                    heightFieldFocusRequester.requestFocus()
                }
            )
        )

        MeasurementField(
            label = "Wzrost",
            value = TextFieldValue(measurements.height, selection = TextRange(measurements.height.length)),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(height = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.height,
            imeAction = ImeAction.Done,
            focusRequester = heightFieldFocusRequester,
            keyboardActions = KeyboardActions(
                onNext = { noteFieldFocusRequester.requestFocus() },
                onDone = { focusManager.clearFocus() }
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Notatka (opcjonalnie)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            BasicTextField(
                value = measurements.note,
                onValueChange = { onMeasurementsChange(measurements.copy(note = it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
                    .focusRequester(noteFieldFocusRequester),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (measurements.note.isEmpty()) {
                            Text(
                                text = "Dodaj notatkę...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun UpperBodyMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val neckFocusRequester = remember { FocusRequester() }
    val bicepsFocusRequester = remember { FocusRequester() }
    val chestFocusRequester = remember { FocusRequester() }
    val beltFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            neckFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Górna część ciała",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MeasurementField(
                label = "Szyja",
                value = TextFieldValue(measurements.neck, selection = TextRange(measurements.neck.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(neck = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.neck,
                imeAction = ImeAction.Next,
                focusRequester = neckFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        bicepsFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Biceps",
                value = TextFieldValue(measurements.biceps, selection = TextRange(measurements.biceps.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(biceps = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.biceps,
                imeAction = ImeAction.Next,
                focusRequester = bicepsFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        chestFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Klatka piersiowa",
                value = TextFieldValue(measurements.chest, selection = TextRange(measurements.chest.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(chest = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.chest,
                imeAction = ImeAction.Next,
                focusRequester = chestFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        beltFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Pas",
                value = TextFieldValue(measurements.belt, selection = TextRange(measurements.belt.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(belt = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.belt,
                imeAction = ImeAction.Done,
                focusRequester = beltFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() },
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}

@Composable
private fun LowerBodyMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false
) {
    val focusManager = LocalFocusManager.current
    val waistFocusRequester = remember { FocusRequester() }
    val hipsFocusRequester = remember { FocusRequester() }
    val thighFocusRequester = remember { FocusRequester() }
    val calfFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            waistFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Dolna część ciała",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MeasurementField(
                label = "Talia",
                value = TextFieldValue(measurements.waist, selection = TextRange(measurements.waist.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(waist = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.waist,
                imeAction = ImeAction.Next,
                focusRequester = waistFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        hipsFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Biodra",
                value = TextFieldValue(measurements.hips, selection = TextRange(measurements.hips.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(hips = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.hips,
                imeAction = ImeAction.Next,
                focusRequester = hipsFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        thighFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Uda",
                value = TextFieldValue(measurements.thigh, selection = TextRange(measurements.thigh.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(thigh = newValue.text)) },
                unit = "cm",
                errorMessage = measurements.validationState.thigh,
                imeAction = ImeAction.Next,
                focusRequester = thighFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = {
                        calfFocusRequester.requestFocus()
                    }
                )
            )

            MeasurementField(
                label = "Łydki",
                value = TextFieldValue(measurements.calf, selection = TextRange(measurements.calf.length)),
                onValueChange = { newValue -> onMeasurementsChange(measurements.copy(calf = newValue.text)) },
                unit = "cm",
                imeAction = ImeAction.Done,
                errorMessage = measurements.validationState.calf,
                focusRequester = calfFocusRequester,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() },
                    onDone = { focusManager.clearFocus() }
                )
            )
        }
    }
}

@Composable
private fun MeasurementField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    unit: String = "cm",
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    focusRequester: FocusRequester = remember { FocusRequester() },
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (errorMessage != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.text.isEmpty() ||
                        (newValue.text.all { it.isDigit() } && newValue.text.length <= 3 && newValue.text.toIntOrNull() != null)
                    ) {
                        onValueChange(newValue.copy(selection = TextRange(newValue.text.length)))
                    }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = imeAction
                ),
                keyboardActions = keyboardActions,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .focusRequester(focusRequester),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.text.isEmpty()) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

private fun BodyMeasurements.toInputState(): MeasurementsInputState {
    return MeasurementsInputState(
        weight = weight.toString(),
        height = height.toString(),
        neck = neck.toString(),
        biceps = biceps.toString(),
        chest = chest.toString(),
        waist = waist.toString(),
        belt = belt.toString(),
        hips = hips.toString(),
        thigh = thigh.toString(),
        calf = calf.toString(),
        note = note
    )
}