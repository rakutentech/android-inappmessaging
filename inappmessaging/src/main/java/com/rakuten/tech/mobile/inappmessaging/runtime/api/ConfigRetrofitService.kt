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
    fun getConfigService(
            @Url url: String,
            @Query("platform", encoded = false) platform: Int,
            @Query("appId", encoded = false) appId: String,
            @Query("sdkVersion", encoded = false) sdkVersion: String,
            @Query("appVersion", encoded = false) appVersion: String,
            @Query("locale", encoded = false) locale: String
    ): Call<ConfigResponse>

}
