package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.UserIdentifierType

/**
 * This class represents user identification.
 */
internal data class UserIdentifier(
    private val idType: UserIdentifierType,
    @SerializedName("id")
    private val id: String
) {

    @SerializedName("type")
    private val type = idType.typeId
}
