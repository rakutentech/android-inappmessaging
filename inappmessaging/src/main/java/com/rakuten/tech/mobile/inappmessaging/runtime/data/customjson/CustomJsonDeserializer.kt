package com.rakuten.tech.mobile.inappmessaging.runtime.data.customjson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger
import java.lang.reflect.Type

internal class CustomJsonDeserializer : JsonDeserializer<CustomJson> {

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): CustomJson {
        val jsonObject = json?.asJsonObject

        val pushPrimer = context?.safeDeserialize<PushPrimer>(jsonObject?.get("pushPrimer"))
        val clickableImage = context?.safeDeserialize<ClickableImage>(jsonObject?.get("clickableImage"))
        val background = context?.safeDeserialize<Background>(jsonObject?.get("background"))

        return CustomJson(pushPrimer, clickableImage, background)
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    private inline fun <reified T> JsonDeserializationContext.safeDeserialize(jsonElement: JsonElement?): T? {
        return try {
            this.deserialize<T>(jsonElement, T::class.java)
        } catch (_: Exception) {
            InAppLogger("IAM_CustomJsonDeserializer")
                .warn("Invalid format for ${T::class.java.name.split(".").lastOrNull()}")
            null
        }
    }
}
