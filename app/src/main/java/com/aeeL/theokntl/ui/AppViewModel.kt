package com.aeeL.theokntl.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aeeL.theokntl.core.isCancellation
import com.aeeL.theokntl.data.log.SessionLog
import com.aeeL.theokntl.data.store.AppPreferences
import com.aeeL.theokntl.domain.model.AppSettings
import com.aeeL.theokntl.domain.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val sessionLog: SessionLog,
) : ViewModel() {
    val settings: StateFlow<AppSettings> = preferences.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    fun setThemeMode(mode: ThemeMode) {
        persist("the theme") { preferences.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        persist("the dynamic color setting") { preferences.setDynamicColor(enabled) }
    }

    fun setDebugDetails(enabled: Boolean) {
        persist("the debug setting") { preferences.setDebugDetails(enabled) }
    }

    private fun persist(label: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (error: Throwable) {
                if (error.isCancellation()) throw error
                sessionLog.info("Could not save $label: ${error.message.orEmpty()}")
            }
        }
    }
}
