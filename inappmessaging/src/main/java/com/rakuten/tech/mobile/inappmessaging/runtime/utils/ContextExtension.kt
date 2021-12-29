package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.app.Activity
import android.view.View

object ContextExtension {
    fun <T: View> Activity.findViewByName(name: String): T? {
        val id = this.resources.getIdentifier(name, "id", this.packageName)
        if (id > 0) {
            return this.findViewById(id)
        }
        return null
    }
}