package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.ValueType
import org.jetbrains.annotations.NotNull
import java.util.Locale

/**
 * This class represents InAppMessaging's custom event attribute.
 */
@RestrictTo(LIBRARY)
data class Attribute(
    @NotNull private val nm: String,
    @NotNull val value: String,
    @NotNull private val type: ValueType
) {
    val name = nm.toLowerCase(Locale.getDefault())
    val valueType = type.typeId
}
