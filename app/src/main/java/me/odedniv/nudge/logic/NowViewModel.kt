package me.odedniv.nudge.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class NowViewModel : ViewModel() {
  val value = MutableStateFlow(Instant.now())
  private var job: Job? = null

  fun start() {
    if (job != null) return
    job =
      viewModelScope.launch {
        while (true) {
          value.value = Instant.now()
          delay(1000)
        }
      }
  }

  fun stop() {
    job?.cancel()
    job = null
  }

  override fun onCleared() {
    super.onCleared()
    stop()
  }
}
