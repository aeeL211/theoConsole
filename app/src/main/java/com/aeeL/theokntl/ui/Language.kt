package com.aeeL.theokntl.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.aeeL.theokntl.domain.model.AppLanguage

fun currentAppLanguage(): AppLanguage {
    val locales = AppCompatDelegate.getApplicationLocales()
    return when (locales.get(0)?.language) {
        "en" -> AppLanguage.ENGLISH
        "id", "in" -> AppLanguage.INDONESIAN
        else -> AppLanguage.SYSTEM
    }
}

fun setAppLanguage(language: AppLanguage) {
    val locales = when (language) {
        AppLanguage.SYSTEM -> LocaleListCompat.getEmptyLocaleList()
        AppLanguage.ENGLISH -> LocaleListCompat.forLanguageTags("en")
        AppLanguage.INDONESIAN -> LocaleListCompat.forLanguageTags("id")
    }
    AppCompatDelegate.setApplicationLocales(locales)
}
