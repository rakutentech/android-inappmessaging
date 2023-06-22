package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.DEVICE_ID_HEADER
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.SUBSCRIPTION_ID_HEADER
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap
import retrofit2.http.Url

/**
 * Retrofit API interface in order for Retrofit to make request to Config Service.
 * Contains endpoint communicating with Config Service.
 */
internal fun interface ConfigRetrofitService {

    /**
     * This method retrieves config.
     */
    @GET
    fun getConfigService(
        @Url url: String,
        @Header(SUBSCRIPTION_ID_HEADER) subscriptionId: String,
        @Header(DEVICE_ID_HEADER) deviceId: String,
        @QueryMap parameters: Map<String, @JvmSuppressWildcards Any?>,
    ): Call<ConfigResponse>
}
