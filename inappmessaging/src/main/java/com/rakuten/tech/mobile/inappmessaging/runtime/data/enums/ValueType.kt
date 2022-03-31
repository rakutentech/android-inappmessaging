package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Data value's type supported by InAppMessaging's custom events.
 *
 * @param typeId Value type id.
 */
@RestrictTo(LIBRARY)
@SuppressWarnings("MagicNumber", "OutdatedDocumentation")
enum class ValueType(@NotNull val typeId: Int) {
    INVALID(0),
    STRING(1),
    INTEGER(2),
    DOUBLE(3),
    BOOLEAN(4),
    TIME_IN_MILLI(5);

    companion object {
        /**
         * Gets the value data type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        @Nullable
        fun getById(@NotNull typeId: Int): ValueType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
