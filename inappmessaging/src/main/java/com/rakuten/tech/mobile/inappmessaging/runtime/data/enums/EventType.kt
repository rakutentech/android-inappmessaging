package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

/**
 * Local events type according to type ID.
 */
@RestrictTo(LIBRARY)
@SuppressWarnings("MagicNumber")
enum class EventType(@NotNull val typeId: Int) {
    INVALID(0),
    APP_START(1),
    LOGIN_SUCCESSFUL(2),
    PURCHASE_SUCCESSFUL(3),
    CUSTOM(4);

    companion object {
        /**
         * Gets the trigger log event type for a given [typeId].
         * If [typeId] argument is not any of the valid id, null will be returned.
         */
        @Nullable
        fun getById(@NotNull typeId: Int): EventType? {
            for (type in values()) {
                if (type.typeId == typeId) {
                    return type
                }
            }
            return null
        }
    }
}
