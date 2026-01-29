package com.noisevisionsoftware.vitema.ui.screens.bodyMeasurements.components

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.exceptions.MeasurementValidation
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementsInputState
import com.noisevisionsoftware.vitema.ui.screens.bodyMeasurements.BodyMeasurementsViewModel
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

    val hasFirstPageErrors = measurements.run {
        validationState.weight != null || validationState.neck != null ||
                validationState.biceps != null
    }

    val hasSecondPageErrors = measurements.run {
        validationState.chest != null || validationState.waist != null ||
                validationState.belt != null
    }

    val hasThirdPageErrors = measurements.run {
        validationState.hips != null || validationState.thigh != null ||
                validationState.calf != null
    }

    LaunchedEffect(Unit) {
        viewModel.getLastMeasurements()?.let { lastMeasurements ->
            if (lastMeasurements.measurementType == MeasurementType.FULL_BODY) {
                measurements = lastMeasurements.toInputState()
            }
        }
    }

    BasicAlertDialog(
        onDismissRequest = {}
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.65f)
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "Dodaj pomiary",
                            style = MaterialTheme.typography.titleMedium
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Zamknij"
                            )
                        }
                    }

                    // Page indicators
                    PageIndicators(
                        pagerState = pagerState,
                        hasBasicErrors = hasFirstPageErrors,
                        hasUpperBodyErrors = hasSecondPageErrors,
                        hasLowerBodyErrors = hasThirdPageErrors,
                        scope = scope,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }

                // Content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> FirstMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 0,
                            modifier = Modifier.fillMaxHeight()
                        )

                        1 -> SecondMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 1,
                            modifier = Modifier.fillMaxHeight()
                        )

                        2 -> ThirdMeasurementsSection(
                            measurements,
                            onMeasurementsChange = { measurements = it },
                            isVisible = currentPage == 2,
                            modifier = Modifier.fillMaxHeight()
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .imePadding(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Anuluj",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Row {
                        if (pagerState.currentPage > 0) {
                            TextButton(onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }) {
                                Text(
                                    text = "Wstecz",
                                    style = MaterialTheme.typography.labelMedium
                                )
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
                                Text(
                                    text = "Dalej",
                                    style = MaterialTheme.typography.labelMedium
                                )
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
                                Text(
                                    text = "Dodaj",
                                    style = MaterialTheme.typography.labelMedium
                                )
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
private fun FirstMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val weightFieldFocusRequester = remember { FocusRequester() }
    val neckFocusRequester = remember { FocusRequester() }
    val bicepsFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            weightFieldFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MeasurementField(
            label = "Waga",
            value = TextFieldValue(
                measurements.weight,
                selection = TextRange(measurements.weight.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(weight = newValue.text)) },
            unit = "kg",
            errorMessage = measurements.validationState.weight,
            imeAction = ImeAction.Next,
            focusRequester = weightFieldFocusRequester,
            keyboardActions = KeyboardActions(
                onNext = {
                    neckFocusRequester.requestFocus()
                }
            )
        )


        MeasurementField(
            label = "Szyja",
            value = TextFieldValue(
                measurements.neck,
                selection = TextRange(measurements.neck.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(neck = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.neck,
            imeAction = ImeAction.Next,
            focusRequester = neckFocusRequester,
            keyboardActions = KeyboardActions(onNext = { bicepsFocusRequester.requestFocus() })
        )

        MeasurementField(
            label = "Biceps",
            value = TextFieldValue(
                measurements.biceps,
                selection = TextRange(measurements.biceps.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(biceps = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.biceps,
            focusRequester = bicepsFocusRequester,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}

@Composable
private fun SecondMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val chestFocusRequester = remember { FocusRequester() }
    val waistFocusRequester = remember { FocusRequester() }
    val beltFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            chestFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MeasurementField(
            label = "Klatka piersiowa",
            value = TextFieldValue(
                measurements.chest,
                selection = TextRange(measurements.chest.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(chest = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.chest,
            imeAction = ImeAction.Next,
            focusRequester = chestFocusRequester,
            keyboardActions = KeyboardActions(onNext = { waistFocusRequester.requestFocus() })
        )

        MeasurementField(
            label = "Talia",
            value = TextFieldValue(
                measurements.waist,
                selection = TextRange(measurements.waist.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(waist = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.waist,
            imeAction = ImeAction.Next,
            focusRequester = waistFocusRequester,
            keyboardActions = KeyboardActions(onNext = { beltFocusRequester.requestFocus() })
        )

        MeasurementField(
            label = "Pas",
            value = TextFieldValue(
                measurements.belt,
                selection = TextRange(measurements.belt.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(belt = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.belt,
            focusRequester = beltFocusRequester,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
    }
}

@Composable
private fun ThirdMeasurementsSection(
    measurements: MeasurementsInputState,
    onMeasurementsChange: (MeasurementsInputState) -> Unit,
    isVisible: Boolean = false,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val hipsFocusRequester = remember { FocusRequester() }
    val thighFocusRequester = remember { FocusRequester() }
    val calfFocusRequester = remember { FocusRequester() }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            hipsFocusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MeasurementField(
            label = "Biodra",
            value = TextFieldValue(
                measurements.hips,
                selection = TextRange(measurements.hips.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(hips = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.hips,
            imeAction = ImeAction.Next,
            focusRequester = hipsFocusRequester,
            keyboardActions = KeyboardActions(onNext = { thighFocusRequester.requestFocus() })
        )

        MeasurementField(
            label = "Uda",
            value = TextFieldValue(
                measurements.thigh,
                selection = TextRange(measurements.thigh.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(thigh = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.thigh,
            imeAction = ImeAction.Next,
            focusRequester = thighFocusRequester,
            keyboardActions = KeyboardActions(onNext = { calfFocusRequester.requestFocus() })
        )

        MeasurementField(
            label = "Łydki",
            value = TextFieldValue(
                measurements.calf,
                selection = TextRange(measurements.calf.length)
            ),
            onValueChange = { newValue -> onMeasurementsChange(measurements.copy(calf = newValue.text)) },
            unit = "cm",
            errorMessage = measurements.validationState.calf,
            focusRequester = calfFocusRequester,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )
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
            style = MaterialTheme.typography.bodySmall,
            color = if (errorMessage != null)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val currentValue = value.text.toIntOrNull() ?: 0
                    if (currentValue > 0) {
                        onValueChange(TextFieldValue((currentValue - 1).toString()))
                    }
                },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                enabled = (value.text.toIntOrNull() ?: 0) > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zmniejsz",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.text.isEmpty() ||
                        (newValue.text.all { it.isDigit() } && newValue.text.length <= 3 && newValue.text.toIntOrNull() != null)
                    ) {
                        onValueChange(newValue.copy(selection = TextRange(newValue.text.length)))
                    }
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
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
                        contentAlignment = Alignment.Center
                    ) {
                        if (value.text.isEmpty()) {
                            Text(
                                text = "0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )

            IconButton(
                onClick = {
                    val currentValue = value.text.toIntOrNull() ?: 0
                    if (currentValue < 999) {
                        onValueChange(TextFieldValue((currentValue + 1).toString()))
                    }
                },
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                ),
                enabled = (value.text.toIntOrNull() ?: 0) < 999
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zwiększ",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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