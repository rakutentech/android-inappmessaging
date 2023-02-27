package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.QueryMap
import retrofit2.http.Url

/**
 * Retrofit API interface in order for Retrofit to make request to Config Service.
 * Contains endpoint communicating with Config Service.
 */
internal interface ConfigRetrofitService {

    /**
     * This method retrieves config.
     */
    @GET
    fun getConfigService(
        @Url url: String,
        @Header("Subscription-Id") subscriptionId: String,
        @QueryMap parameters: Map<String, @JvmSuppressWildcards Any?>,
    ): Call<ConfigResponse>
}
