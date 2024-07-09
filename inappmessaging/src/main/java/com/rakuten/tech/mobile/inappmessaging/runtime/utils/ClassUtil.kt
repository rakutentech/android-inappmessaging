package com.rakuten.tech.mobile.inappmessaging.runtime.utils

internal object ClassUtil {

    @SuppressWarnings("SwallowedException")
    @JvmStatic
    fun hasClass(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
