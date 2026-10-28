package com.aeeL.theokntl.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import com.aeeL.theokntl.domain.model.AppLanguage
import java.util.Locale

private const val LANGUAGE_PREFS = "theo_language"
private const val LANGUAGE_KEY = "language"

fun currentAppLanguage(context: Context): AppLanguage {
    val stored = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        .getString(LANGUAGE_KEY, null)
    return when (stored) {
        "en" -> AppLanguage.ENGLISH
        "id", "in" -> AppLanguage.INDONESIAN
        else -> AppLanguage.SYSTEM
    }
}

fun setAppLanguage(context: Context, language: AppLanguage) {
    if (currentAppLanguage(context) == language) return
    val editor = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE).edit()
    when (language) {
        AppLanguage.SYSTEM -> editor.remove(LANGUAGE_KEY)
        AppLanguage.ENGLISH -> editor.putString(LANGUAGE_KEY, "en")
        AppLanguage.INDONESIAN -> editor.putString(LANGUAGE_KEY, "id")
    }
    editor.apply()
    context.findActivity()?.recreate()
}

fun localizedContext(base: Context): Context {
    val tag = when (currentAppLanguage(base)) {
        AppLanguage.SYSTEM -> return base
        AppLanguage.ENGLISH -> "en"
        AppLanguage.INDONESIAN -> "id"
    }
    val locale = Locale.forLanguageTag(tag)
    val configuration = Configuration(base.resources.configuration).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    return base.createConfigurationContext(configuration)
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
