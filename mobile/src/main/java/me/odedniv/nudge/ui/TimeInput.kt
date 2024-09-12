/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalMaterial3Api::class)

package me.odedniv.nudge.ui

import android.text.format.DateFormat.is24HourFormat
import androidx.annotation.IntRange
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.zIndex
import java.text.NumberFormat
import java.util.Locale
import java.util.WeakHashMap
import me.odedniv.nudge.R

/* Copied from Compose TimeInput, but supports seconds. */

@Composable
fun TimeInput(
  state: TimePickerState,
  modifier: Modifier = Modifier,
  colors: TimePickerColors = TimePickerDefaults.colors(),
) {
  TimeInputImpl(modifier, colors, state)
}

/**
 * Creates a [TimePickerState] for a time picker that is remembered across compositions and
 * configuration changes.
 *
 * @param initialHour starting hour for this state, will be displayed in the time picker when
 *   launched. Ranges from 0 to 23
 * @param initialMinute starting minute for this state, will be displayed in the time picker when
 *   launched. Ranges from 0 to 59
 * @param is24Hour The format for this time picker. `false` for 12 hour format with an AM/PM toggle
 *   or `true` for 24 hour format without toggle. Defaults to follow system setting.
 */
@Composable
@ExperimentalMaterial3Api
fun rememberTimePickerState(
  initialHour: Int = 0,
  initialMinute: Int = 0,
  initialSecond: Int = 0,
  showSeconds: Boolean = false,
  is24Hour: Boolean = showSeconds || is24HourFormat,
): TimePickerState {
  val state: TimePickerStateImpl =
    rememberSaveable(saver = TimePickerStateImpl.Saver()) {
      TimePickerStateImpl(
        initialHour = initialHour,
        initialMinute = initialMinute,
        initialSecond = initialSecond,
        is24Hour = is24Hour,
        showSeconds = showSeconds,
      )
    }

  return state
}

interface TimePickerState {

  /** The currently selected second (0-59). */
  @get:IntRange(from = 0, to = 59) @setparam:IntRange(from = 0, to = 59) var second: Int

  /** The currently selected minute (0-59). */
  @get:IntRange(from = 0, to = 59) @setparam:IntRange(from = 0, to = 59) var minute: Int

  /** The currently selected hour (0-23). */
  @get:IntRange(from = 0, to = 23) @setparam:IntRange(from = 0, to = 23) var hour: Int

  /** Indicates whether the time picker is showing seconds. */
  var showSeconds: Boolean

  /**
   * Indicates whether the time picker uses 24-hour format (`true`) or 12-hour format with AM/PM
   * (`false`).
   */
  var is24hour: Boolean

  /** Specifies whether the hour or minute component is being actively selected by the user. */
  var selection: TimePickerSelectionMode

  /** Indicates whether the selected time falls within the afternoon period (12 PM - 12 AM). */
  var isAfternoon: Boolean
}

/** The selection mode for the time picker */
@JvmInline
value class TimePickerSelectionMode private constructor(val value: Int) {
  companion object {
    val Hour = TimePickerSelectionMode(0)
    val Minute = TimePickerSelectionMode(1)
    val Second = TimePickerSelectionMode(2)
  }

  override fun toString(): String =
    when (this) {
      Hour -> "Hour"
      Minute -> "Minute"
      Second -> "Second"
      else -> ""
    }

  val stringResourceId
    get(): Int =
      when (this) {
        Hour -> R.string.time_hour
        Minute -> R.string.time_minute
        Second -> R.string.time_second
        else -> R.string.time_second
      }
}

private class TimePickerStateImpl(
  initialHour: Int,
  initialMinute: Int,
  initialSecond: Int,
  override var showSeconds: Boolean,
  is24Hour: Boolean,
) : TimePickerState {
  init {
    require(initialHour in 0..23) { "initialHour should in [0..23] range" }
    require(initialMinute in 0..59) { "initialMinute should be in [0..59] range" }
    require(initialSecond in 0..59) { "initialSecond should be in [0..59] range" }
    require(!showSeconds || is24Hour) { "is24Hour must be true if showSeconds is true" }
  }

  override var is24hour: Boolean = is24Hour

  override var selection by mutableStateOf(TimePickerSelectionMode.Hour)

  override var isAfternoon by mutableStateOf(initialHour >= 12)

  val hourState = mutableIntStateOf(initialHour % 12)

  val minuteState = mutableIntStateOf(initialMinute)

  val secondState = mutableIntStateOf(initialSecond)

  override var second: Int
    get() = secondState.intValue
    set(value) {
      secondState.intValue = value
    }

  override var minute: Int
    get() = minuteState.intValue
    set(value) {
      minuteState.intValue = value
    }

  override var hour: Int
    get() = hourState.intValue + if (isAfternoon) 12 else 0
    set(value) {
      isAfternoon = value >= 12
      hourState.intValue = value % 12
    }

  companion object {
    /** The default [Saver] implementation for [TimePickerState]. */
    fun Saver(): Saver<TimePickerStateImpl, *> =
      Saver(
        save = { listOf(it.hour, it.minute, it.second, it.showSeconds, it.is24hour) },
        restore = { value ->
          TimePickerStateImpl(
            initialHour = value[0] as Int,
            initialMinute = value[1] as Int,
            initialSecond = value[2] as Int,
            showSeconds = value[3] as Boolean,
            is24Hour = value[4] as Boolean,
          )
        },
      )
  }
}

private val TimePickerState.hourForDisplay: Int
  get() =
    when {
      is24hour -> hour % 24
      hour % 12 == 0 -> 12
      isAfternoon -> hour - 12
      else -> hour
    }

@Composable
private fun TimeInputImpl(modifier: Modifier, colors: TimePickerColors, state: TimePickerState) {
  var hourValue by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue(text = state.hourForDisplay.toLocalString(minDigits = 2)))
    }
  var minuteValue by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue(text = state.minute.toLocalString(minDigits = 2)))
    }
  var secondValue by
    rememberSaveable(stateSaver = TextFieldValue.Saver) {
      mutableStateOf(TextFieldValue(text = state.second.toLocalString(minDigits = 2)))
    }
  Row(
    modifier = modifier.padding(bottom = TimeInputBottomPadding),
    verticalAlignment = Alignment.Top,
  ) {
    val textStyle =
      TimeInputTokens.TimeFieldLabelTextFont.value.copy(
        textAlign = TextAlign.Center,
        color = colors.timeSelectorContentColor(true),
      )

    CompositionLocalProvider(
      LocalTextStyle provides textStyle,
      // Always display the time input text field from left to right.
      LocalLayoutDirection provides LayoutDirection.Ltr,
    ) {
      Row textFieldRow@{
        TimePickerTextField(
          modifier =
            Modifier.onKeyEvent { event ->
              // Zero == 48, Nine == 57
              val switchFocus =
                event.utf16CodePoint in 48..57 &&
                  hourValue.selection.start == 2 &&
                  hourValue.text.length == 2

              if (switchFocus) {
                state.selection = TimePickerSelectionMode.Minute
              }

              false
            },
          value = hourValue,
          onValueChange = { newValue ->
            timeInputOnChange(
              selection = TimePickerSelectionMode.Hour,
              state = state,
              value = newValue,
              prevValue = hourValue,
              max = if (state.is24hour) 23 else 12,
            ) {
              hourValue = it
            }
          },
          state = state,
          selection = TimePickerSelectionMode.Hour,
          keyboardOptions =
            KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
          keyboardActions =
            KeyboardActions(onNext = { state.selection = TimePickerSelectionMode.Minute }),
          colors = colors,
        )
        DisplaySeparator(
          Modifier.size(DisplaySeparatorWidth, TimeInputTokens.PeriodSelectorContainerHeight)
        )
        TimePickerTextField(
          modifier =
            Modifier.onPreviewKeyEvent { event ->
              // Zero == 48, Nine == 57
              val switchFocus =
                if (state.showSeconds) {
                  event.utf16CodePoint in 48..57 &&
                    minuteValue.selection.start == 2 &&
                    minuteValue.text.length == 2
                } else {
                  event.utf16CodePoint == 0 && secondValue.selection.start == 0
                }

              if (switchFocus) {
                state.selection =
                  if (state.showSeconds) TimePickerSelectionMode.Second
                  else TimePickerSelectionMode.Hour
              }

              switchFocus
            },
          value = minuteValue,
          onValueChange = { newValue ->
            timeInputOnChange(
              selection = TimePickerSelectionMode.Minute,
              state = state,
              value = newValue,
              prevValue = minuteValue,
              max = 59,
            ) {
              minuteValue = it
            }
          },
          state = state,
          selection = TimePickerSelectionMode.Minute,
          keyboardOptions =
            KeyboardOptions(
              imeAction = if (state.showSeconds) ImeAction.Next else ImeAction.Done,
              keyboardType = KeyboardType.Number,
            ),
          keyboardActions =
            KeyboardActions(
              onNext = {
                state.selection =
                  if (state.showSeconds) TimePickerSelectionMode.Second
                  else TimePickerSelectionMode.Hour
              }
            ),
          colors = colors,
        )
        if (!state.showSeconds) return@textFieldRow
        DisplaySeparator(
          Modifier.size(DisplaySeparatorWidth, TimeInputTokens.PeriodSelectorContainerHeight)
        )
        TimePickerTextField(
          modifier =
            Modifier.onPreviewKeyEvent { event ->
              // 0 == KEYCODE_DEL
              val switchFocus = event.utf16CodePoint == 0 && secondValue.selection.start == 0

              if (switchFocus) {
                state.selection = TimePickerSelectionMode.Hour
              }

              switchFocus
            },
          value = secondValue,
          onValueChange = { newValue ->
            timeInputOnChange(
              selection = TimePickerSelectionMode.Second,
              state = state,
              value = newValue,
              prevValue = secondValue,
              max = 59,
            ) {
              secondValue = it
            }
          },
          state = state,
          selection = TimePickerSelectionMode.Second,
          keyboardOptions =
            KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
          keyboardActions =
            KeyboardActions(onNext = { state.selection = TimePickerSelectionMode.Hour }),
          colors = colors,
        )
      }
    }

    if (!state.is24hour) {
      Box(Modifier.padding(start = PeriodToggleMargin)) {
        VerticalPeriodToggle(
          modifier =
            Modifier.size(
              TimeInputTokens.PeriodSelectorContainerWidth,
              TimeInputTokens.PeriodSelectorContainerHeight,
            ),
          state = state,
          colors = colors,
        )
      }
    }
  }
}

@Composable
private fun VerticalPeriodToggle(
  modifier: Modifier,
  state: TimePickerState,
  colors: TimePickerColors,
) {
  val measurePolicy = remember {
    MeasurePolicy { measurables, constraints ->
      val spacer = measurables.fastFirst { it.layoutId == "Spacer" }
      val spacerPlaceable =
        spacer.measure(
          constraints.copy(
            minHeight = 0,
            maxHeight = TimeInputTokens.PeriodSelectorOutlineWidth.roundToPx(),
          )
        )

      val items =
        measurables
          .fastFilter { it.layoutId != "Spacer" }
          .fastMap { item ->
            item.measure(constraints.copy(minHeight = 0, maxHeight = constraints.maxHeight / 2))
          }

      layout(constraints.maxWidth, constraints.maxHeight) {
        items[0].place(0, 0)
        items[1].place(0, items[0].height)
        spacerPlaceable.place(0, items[0].height - spacerPlaceable.height / 2)
      }
    }
  }

  val shape = TimeInputTokens.PeriodSelectorContainerShape.value as CornerBasedShape

  PeriodToggleImpl(
    modifier = modifier,
    state = state,
    colors = colors,
    measurePolicy = measurePolicy,
    startShape = shape.top(),
    endShape = shape.bottom(),
  )
}

@Composable
private fun PeriodToggleImpl(
  modifier: Modifier,
  state: TimePickerState,
  colors: TimePickerColors,
  measurePolicy: MeasurePolicy,
  startShape: Shape,
  endShape: Shape,
) {
  val borderStroke =
    BorderStroke(TimeInputTokens.PeriodSelectorOutlineWidth, colors.periodSelectorBorderColor)

  val shape = TimeInputTokens.PeriodSelectorContainerShape.value as CornerBasedShape
  val contentDescription = stringResource(R.string.time_period_toggle)
  Layout(
    modifier =
      modifier
        .semantics {
          isTraversalGroup = true
          this.contentDescription = contentDescription
        }
        .selectableGroup()
        .border(border = borderStroke, shape = shape),
    measurePolicy = measurePolicy,
    content = {
      ToggleItem(
        checked = !state.isAfternoon,
        shape = startShape,
        onClick = { state.isAfternoon = false },
        colors = colors,
      ) {
        Text(text = stringResource(R.string.time_period_am))
      }
      Spacer(
        Modifier.layoutId("Spacer")
          .zIndex(SeparatorZIndex)
          .fillMaxSize()
          .background(color = colors.periodSelectorBorderColor)
      )
      ToggleItem(
        checked = state.isAfternoon,
        shape = endShape,
        onClick = { state.isAfternoon = true },
        colors = colors,
      ) {
        Text(text = stringResource(R.string.time_period_pm))
      }
    },
  )
}

@Composable
private fun ToggleItem(
  checked: Boolean,
  shape: Shape,
  onClick: () -> Unit,
  colors: TimePickerColors,
  content: @Composable RowScope.() -> Unit,
) {
  val contentColor = colors.periodSelectorContentColor(checked)
  val containerColor = colors.periodSelectorContainerColor(checked)

  TextButton(
    modifier =
      Modifier.zIndex(if (checked) 0f else 1f).fillMaxSize().semantics { selected = checked },
    contentPadding = PaddingValues(0.dp),
    shape = shape,
    onClick = onClick,
    content = content,
    colors =
      ButtonDefaults.textButtonColors(contentColor = contentColor, containerColor = containerColor),
  )
}

@Composable
private fun DisplaySeparator(modifier: Modifier) {
  val style =
    LocalTextStyle.current.copy(
      textAlign = TextAlign.Center,
      lineHeightStyle =
        LineHeightStyle(
          alignment = LineHeightStyle.Alignment.Center,
          trim = LineHeightStyle.Trim.Both,
        ),
    )

  Box(modifier = modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
    Text(text = ":", color = TimeInputTokens.TimeFieldSeparatorColor.value, style = style)
  }
}

@Composable
private fun TimeSelector(
  modifier: Modifier,
  value: Int,
  state: TimePickerState,
  selection: TimePickerSelectionMode,
  colors: TimePickerColors,
) {
  val selected = state.selection == selection
  val selectorContentDescription = stringResource(selection.stringResourceId)

  val containerColor = colors.timeSelectorContainerColor(selected)
  val contentColor = colors.timeSelectorContentColor(selected)
  Surface(
    modifier =
      modifier.semantics(mergeDescendants = true) {
        role = Role.RadioButton
        this.contentDescription = selectorContentDescription
      },
    onClick = {
      if (selection != state.selection) {
        state.selection = selection
      }
    },
    selected = selected,
    shape = TimeInputTokens.TimeSelectorContainerShape.value,
    color = containerColor,
  ) {
    val valueContentDescription =
      numberContentDescription(selection = selection, is24Hour = state.is24hour, number = value)

    Box(contentAlignment = Alignment.Center) {
      Text(
        modifier = Modifier.semantics { contentDescription = valueContentDescription },
        text = value.toLocalString(minDigits = 2),
        color = contentColor,
      )
    }
  }
}

private fun timeInputOnChange(
  selection: TimePickerSelectionMode,
  state: TimePickerState,
  value: TextFieldValue,
  prevValue: TextFieldValue,
  max: Int,
  onNewValue: (value: TextFieldValue) -> Unit,
) {
  if (value.text == prevValue.text) {
    // just selection change
    onNewValue(value)
    return
  }

  if (value.text.isEmpty()) {
    when (selection) {
      TimePickerSelectionMode.Hour -> state.hour = 0
      TimePickerSelectionMode.Minute -> state.minute = 0
      TimePickerSelectionMode.Second -> state.second = 0
    }
    onNewValue(value.copy(text = ""))
    return
  }

  try {
    val newValue =
      if (value.text.length == 3 && value.selection.start == 1) {
        value.text[0].digitToInt()
      } else {
        value.text.toInt()
      }

    if (newValue <= max) {
      when (selection) {
        TimePickerSelectionMode.Hour -> {
          state.hour = newValue
          if (newValue > 1 && !state.is24hour) {
            state.selection = TimePickerSelectionMode.Minute
          }
        }
        TimePickerSelectionMode.Minute -> state.minute = newValue
        TimePickerSelectionMode.Second -> state.second = newValue
      }

      onNewValue(
        if (value.text.length <= 2) {
          value
        } else {
          value.copy(text = value.text[0].toString())
        }
      )
    }
  } catch (_: NumberFormatException) {} catch (_: IllegalArgumentException) {
    // do nothing no state update
  }
}

@Composable
private fun TimePickerTextField(
  modifier: Modifier,
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  state: TimePickerState,
  selection: TimePickerSelectionMode,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  colors: TimePickerColors,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val focusRequester = remember { FocusRequester() }
  val textFieldColors =
    OutlinedTextFieldDefaults.colors(
      focusedContainerColor = colors.timeSelectorContainerColor(true),
      unfocusedContainerColor = colors.timeSelectorContainerColor(true),
      focusedTextColor = colors.timeSelectorContentColor(true),
    )
  val selected = selection == state.selection
  Column(modifier = modifier) {
    if (!selected) {
      TimeSelector(
        modifier =
          Modifier.size(
            TimeInputTokens.TimeFieldContainerWidth,
            TimeInputTokens.TimeFieldContainerHeight,
          ),
        value =
          when (selection) {
            TimePickerSelectionMode.Hour -> state.hourForDisplay
            TimePickerSelectionMode.Minute -> state.minute
            TimePickerSelectionMode.Second -> state.second
            else -> state.second
          },
        state = state,
        selection = selection,
        colors = colors,
      )
    }

    val contentDescription = stringResource(selection.stringResourceId)

    Box(Modifier.visible(selected)) {
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
          Modifier.focusRequester(focusRequester)
            .size(TimeInputTokens.TimeFieldContainerWidth, TimeInputTokens.TimeFieldContainerHeight)
            .semantics { this.contentDescription = contentDescription },
        interactionSource = interactionSource,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = LocalTextStyle.current,
        enabled = true,
        singleLine = true,
        cursorBrush =
          Brush.verticalGradient(
            0.00f to Color.Transparent,
            0.10f to Color.Transparent,
            0.10f to MaterialTheme.colorScheme.primary,
            0.90f to MaterialTheme.colorScheme.primary,
            0.90f to Color.Transparent,
            1.00f to Color.Transparent,
          ),
      ) {
        OutlinedTextFieldDefaults.DecorationBox(
          value = value.text,
          visualTransformation = VisualTransformation.None,
          innerTextField = it,
          singleLine = true,
          colors = textFieldColors,
          enabled = true,
          interactionSource = interactionSource,
          contentPadding = PaddingValues(0.dp),
          container = {
            OutlinedTextFieldDefaults.Container(
              enabled = true,
              isError = false,
              interactionSource = interactionSource,
              shape = TimeInputTokens.TimeFieldContainerShape.value,
              colors = textFieldColors,
            )
          },
        )
      }
    }

    Text(
      modifier = Modifier.offset(y = SupportLabelTop).clearAndSetSemantics {},
      text = stringResource(selection.stringResourceId),
      color = TimeInputTokens.TimeFieldSupportingTextColor.value,
      style = TimeInputTokens.TimeFieldSupportingTextFont.value,
    )
  }

  LaunchedEffect(state.selection) {
    if (state.selection == selection) {
      focusRequester.requestFocus()
    }
  }
}

@Stable
private fun TimePickerColors.periodSelectorContainerColor(selected: Boolean) =
  if (selected) {
    periodSelectorSelectedContainerColor
  } else {
    periodSelectorUnselectedContainerColor
  }

@Stable
internal fun TimePickerColors.periodSelectorContentColor(selected: Boolean) =
  if (selected) {
    periodSelectorSelectedContentColor
  } else {
    periodSelectorUnselectedContentColor
  }

@Stable
private fun TimePickerColors.timeSelectorContainerColor(selected: Boolean) =
  if (selected) {
    timeSelectorSelectedContainerColor
  } else {
    timeSelectorUnselectedContainerColor
  }

@Stable
private fun TimePickerColors.timeSelectorContentColor(selected: Boolean) =
  if (selected) {
    timeSelectorSelectedContentColor
  } else {
    timeSelectorUnselectedContentColor
  }

@Composable
@ReadOnlyComposable
internal fun numberContentDescription(
  selection: TimePickerSelectionMode,
  is24Hour: Boolean,
  number: Int,
): String {
  val id =
    when (selection) {
      TimePickerSelectionMode.Hour ->
        if (is24Hour) R.string.time_hour_24h_suffix else R.string.time_hour_suffix
      TimePickerSelectionMode.Minute -> R.string.time_minute_suffix
      TimePickerSelectionMode.Second -> R.string.time_second_suffix
      else -> R.string.time_second_suffix
    }

  return stringResource(id, number)
}

/**
 * Measure the composable with 0,0 so that it stays on the screen. Necessary to correctly handle
 * focus
 */
@Stable
private fun Modifier.visible(visible: Boolean) =
  this.then(
    VisibleModifier(
      visible,
      debugInspectorInfo {
        name = "visible"
        properties["visible"] = visible
      },
    )
  )

private class VisibleModifier(val visible: Boolean, inspectorInfo: InspectorInfo.() -> Unit) :
  LayoutModifier, InspectorValueInfo(inspectorInfo) {

  override fun MeasureScope.measure(
    measurable: Measurable,
    constraints: Constraints,
  ): MeasureResult {
    val placeable = measurable.measure(constraints)

    if (!visible) {
      return layout(0, 0) {}
    }
    return layout(placeable.width, placeable.height) { placeable.place(0, 0) }
  }

  override fun hashCode(): Int = visible.hashCode()

  override fun equals(other: Any?): Boolean {
    val otherModifier = other as? VisibleModifier ?: return false
    return visible == otherModifier.visible
  }
}

private val DisplaySeparatorWidth = 24.dp
private val TimeInputBottomPadding = 24.dp
private val PeriodToggleMargin = 12.dp
private const val SeparatorZIndex = 2f
private val SupportLabelTop = 7.dp

/* From various files. */

private val is24HourFormat: Boolean
  @Composable @ReadOnlyComposable get() = is24HourFormat(LocalContext.current)

private fun Int.toLocalString(
  minDigits: Int,
  maxDigits: Int = minDigits,
  isGroupingUsed: Boolean = false,
): String {
  return getCachedDateTimeFormatter(
      minDigits = minDigits,
      maxDigits = maxDigits,
      isGroupingUsed = isGroupingUsed,
    )
    .format(this)
}

private val cachedFormatters = WeakHashMap<String, NumberFormat>()

private fun getCachedDateTimeFormatter(
  minDigits: Int,
  maxDigits: Int,
  isGroupingUsed: Boolean,
): NumberFormat {
  // Note: Using Locale.getDefault() as a best effort to obtain a unique key and keeping this
  // function non-composable.
  val key = "$minDigits.$maxDigits.$isGroupingUsed.${Locale.getDefault().toLanguageTag()}"
  return cachedFormatters.getOrPut(key) {
    NumberFormat.getIntegerInstance().apply {
      this.isGroupingUsed = isGroupingUsed
      this.minimumIntegerDigits = minDigits
      this.maximumIntegerDigits = maxDigits
    }
  }
}

/** [androidx.compose.material3.tokens.TimeInputTokens] */
private object TimeInputTokens {
  val PeriodSelectorContainerHeight = 72.0.dp
  val PeriodSelectorContainerWidth = 52.0.dp
  val PeriodSelectorContainerShape = ShapeKeyTokens.CornerSmall
  val TimeFieldLabelTextFont = TypographyKeyTokens.DisplayMedium
  val TimeFieldContainerHeight = 72.0.dp
  val TimeFieldContainerShape = ShapeKeyTokens.CornerSmall
  val TimeFieldContainerWidth = 72.0.dp
  val TimeFieldSupportingTextColor = ColorSchemeKeyTokens.OnSurfaceVariant
  val PeriodSelectorOutlineWidth = 1.0.dp
  val TimeFieldSeparatorColor = ColorSchemeKeyTokens.OnSurface
  val TimeFieldSupportingTextFont = TypographyKeyTokens.BodySmall
  val TimeSelectorContainerShape = ShapeKeyTokens.CornerSmall
}

/** [Typography.fromToken] */
private fun Typography.fromToken(value: TypographyKeyTokens): TextStyle {
  return when (value) {
    TypographyKeyTokens.DisplayMedium -> displayMedium
    TypographyKeyTokens.BodySmall -> bodySmall
  }
}

/** [androidx.compose.material3.tokens.TypographyKeyTokens.value] */
private val TypographyKeyTokens.value: TextStyle
  @Composable @ReadOnlyComposable get() = MaterialTheme.typography.fromToken(this)

/** [androidx.compose.material3.tokens.TypographyKeyTokens] */
private enum class TypographyKeyTokens {
  BodySmall,
  DisplayMedium,
}

/** [Shapes.fromToken] */
private fun Shapes.fromToken(value: ShapeKeyTokens): Shape {
  return when (value) {
    ShapeKeyTokens.CornerSmall -> small
  }
}

/** [androidx.compose.material3.tokens.ShapeKeyTokens.value] */
private val ShapeKeyTokens.value: Shape
  @Composable @ReadOnlyComposable get() = MaterialTheme.shapes.fromToken(this)

/** [androidx.compose.material3.tokens.ShapeKeyTokens] */
private enum class ShapeKeyTokens {
  CornerSmall
}

/** [ColorScheme.fromToken] */
private fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color {
  return when (value) {
    ColorSchemeKeyTokens.OnSurface -> onSurface
    ColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
  }
}

/** [androidx.compose.material3.tokens.ColorSchemeKeyTokens.value] */
private val ColorSchemeKeyTokens.value: Color
  @ReadOnlyComposable @Composable get() = MaterialTheme.colorScheme.fromToken(this)

/** [androidx.compose.material3.tokens.ColorSchemeKeyTokens] */
private enum class ColorSchemeKeyTokens {
  OnSurface,
  OnSurfaceVariant,
}

/** Helper function for component shape tokens. Used to grab the top values of a shape parameter. */
private fun CornerBasedShape.top(): CornerBasedShape {
  return copy(bottomStart = CornerSize(0.0.dp), bottomEnd = CornerSize(0.0.dp))
}

/**
 * Helper function for component shape tokens. Used to grab the bottom values of a shape parameter.
 */
private fun CornerBasedShape.bottom(): CornerBasedShape {
  return copy(topStart = CornerSize(0.0.dp), topEnd = CornerSize(0.0.dp))
}
