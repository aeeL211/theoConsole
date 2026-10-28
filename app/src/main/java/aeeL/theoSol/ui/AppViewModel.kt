package aeeL.theoSol.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aeeL.theoSol.core.isCancellation
import aeeL.theoSol.data.log.SessionLog
import aeeL.theoSol.data.store.AppPreferences
import aeeL.theoSol.domain.model.AppSettings
import aeeL.theoSol.domain.model.ThemeMode
import aeeL.theoSol.domain.model.ThemePalette
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

    fun setPalette(palette: ThemePalette) {
        persist("the theme palette") {
            preferences.setPalette(palette)
            preferences.setDynamicColor(false)
        }
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
