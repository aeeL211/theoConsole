package aeeL.theoSol.ui

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import aeeL.theoSol.domain.model.AppLanguage
import java.util.Locale

private const val LANGUAGE_PREFS = "theo_language"
private const val LANGUAGE_KEY = "language"

fun currentAppLanguage(context: Context): AppLanguage {
    val stored = context.applicationContext
        .getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        .getString(LANGUAGE_KEY, null)
    return when (stored) {
        "en" -> AppLanguage.ENGLISH
        "id", "in" -> AppLanguage.INDONESIAN
        else -> AppLanguage.SYSTEM
    }
}

fun setAppLanguage(context: Context, language: AppLanguage) {
    val editor = context.applicationContext
        .getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        .edit()
    when (language) {
        AppLanguage.SYSTEM -> editor.remove(LANGUAGE_KEY)
        AppLanguage.ENGLISH -> editor.putString(LANGUAGE_KEY, "en")
        AppLanguage.INDONESIAN -> editor.putString(LANGUAGE_KEY, "id")
    }
    editor.apply()
}

/** Device locale, independent from a language override stored by the app. */
fun systemLocale(): Locale {
    val locales = Resources.getSystem().configuration.locales
    return if (locales.size() > 0) locales[0] else Locale.getDefault()
}

fun localeFor(language: AppLanguage): Locale = when (language) {
    AppLanguage.SYSTEM -> systemLocale()
    AppLanguage.ENGLISH -> Locale.forLanguageTag("en")
    AppLanguage.INDONESIAN -> Locale.forLanguageTag("id-ID")
}

fun localizedContext(base: Context, language: AppLanguage): Context {
    val locale = localeFor(language)
    val configuration = Configuration(base.resources.configuration).apply {
        setLocales(LocaleList(locale))
        setLayoutDirection(locale)
    }
    return base.createConfigurationContext(configuration)
}

fun localizedContext(base: Context): Context =
    localizedContext(base, currentAppLanguage(base))
