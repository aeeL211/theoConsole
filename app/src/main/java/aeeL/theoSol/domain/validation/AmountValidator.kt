package aeeL.theoSol.domain.validation

enum class AmountError {
    EMPTY,
    NOT_INTEGER,
    TOO_SMALL,
    TOO_LARGE,
}

data class AmountValidation(
    val value: Long?,
    val error: AmountError?,
)

object AmountValidator {
    fun validate(raw: String, roomLeft: Long): AmountValidation {
        val text = raw.trim().replace(Regex("[,_\\s]"), "")
        if (text.isEmpty()) return AmountValidation(null, AmountError.EMPTY)
        if (!text.all { it in '0'..'9' }) return AmountValidation(null, AmountError.NOT_INTEGER)
        val value = text.toLongOrNull() ?: return AmountValidation(null, AmountError.NOT_INTEGER)
        if (value < 1L) return AmountValidation(value, AmountError.TOO_SMALL)
        if (value > roomLeft) return AmountValidation(value, AmountError.TOO_LARGE)
        return AmountValidation(value, null)
    }
}
