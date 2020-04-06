package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * OperatorType contains all the possible enum types for trigger attribute comparison.
 */
@Suppress("MagicNumber")
internal enum class OperatorType(val typeId: Int) {
    INVALID(0),
    EQUALS(1),
    DOES_NOT_EQUAL(2),
    GREATER_THAN(3),
    LESS_THAN(4),
    IS_BLANK(5),
    IS_NOT_BLANK(6),
    MATCHES_REGEX(7),
    DOES_NOT_MATCH_REGEX(8);

    companion object {
        /**
         * Gets the operator type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        fun getById(typeId: Int): OperatorType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
