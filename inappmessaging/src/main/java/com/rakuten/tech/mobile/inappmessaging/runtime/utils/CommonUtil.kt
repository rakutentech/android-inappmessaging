package com.rakuten.tech.mobile.inappmessaging.runtime.utils

/**
 * Class containing common utility methods.
 */
internal object CommonUtil {

    private const val TAG = "CommonUtil"

    fun hasClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            InAppLogger(TAG).info(e.message)
            false
        }
    }
}
