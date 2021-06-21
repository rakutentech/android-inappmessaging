package com.rakuten.tech.mobile.inappmessaging.runtime.data.enums

/**
 * All types of user identifying data types.
 */
@SuppressWarnings("MagicNumber")
internal enum class UserIdentifierType(val typeId: Int) {
    INVALID(0),
    R_ID(1),
    EASY_ID(2),
    USER_ID(3);
}
