package aeeL.theoSol.ui.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

enum class HapticEffect {
    TAP,
    SELECT,
    CONFIRM,
    REJECT,
}

class Haptics internal constructor(private val view: View) {
    fun perform(effect: HapticEffect) {
        view.performHapticFeedback(effect.constant())
    }

    private fun HapticEffect.constant(): Int = when (this) {
        HapticEffect.TAP -> HapticFeedbackConstants.CONTEXT_CLICK
        HapticEffect.SELECT -> HapticFeedbackConstants.CLOCK_TICK
        HapticEffect.CONFIRM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
        HapticEffect.REJECT -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.REJECT
        } else {
            HapticFeedbackConstants.LONG_PRESS
        }
    }
}

@Composable
fun rememberHaptics(): Haptics {
    val view = LocalView.current
    return remember(view) { Haptics(view) }
}
