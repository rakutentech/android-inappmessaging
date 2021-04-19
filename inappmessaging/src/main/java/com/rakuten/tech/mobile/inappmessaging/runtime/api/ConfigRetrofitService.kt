package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Retrofit API interface in order for Retrofit to make request to Config Service.
 * Contains endpoint communicating with Config Service.
 */
internal interface ConfigRetrofitService {

    /**
     * This method retrieves config.
     */
    @GET
    fun getConfigService(@Url url: String, @QueryMap(encoded = false) requestParameters: Map<String, Any?>): Call<ConfigResponse>
}
