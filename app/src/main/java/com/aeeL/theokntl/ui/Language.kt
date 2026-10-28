package com.aeeL.theokntl.ui

import android.content.Context
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
}

fun localizedContext(base: Context, language: AppLanguage): Context {
    val source = base.applicationContext.resources.configuration
    val locale = when (language) {
        AppLanguage.SYSTEM -> source.locales[0] ?: Locale.getDefault()
        AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
        AppLanguage.INDONESIAN -> Locale.forLanguageTag("id")
    }
    val configuration = Configuration(source).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }
    return base.createConfigurationContext(configuration)
}

fun localizedContext(base: Context): Context =
    localizedContext(base, currentAppLanguage(base))
