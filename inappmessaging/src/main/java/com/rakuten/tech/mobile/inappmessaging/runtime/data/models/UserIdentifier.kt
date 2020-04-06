package com.rakuten.tech.mobile.inappmessaging.runtime.data.models

import com.google.gson.annotations.SerializedName
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.UserIdentifierType
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings

/**
 * This class represents user identification.
 */
internal data class UserIdentifier(
    private val idType: UserIdentifierType,
    @property:SuppressFBWarnings("URF_UNREAD_FIELD")
    @SerializedName("id")
    private val id: String
) {

    @SuppressFBWarnings("URF_UNREAD_FIELD")
    @SerializedName("type")
    private val type = idType.typeId
}
