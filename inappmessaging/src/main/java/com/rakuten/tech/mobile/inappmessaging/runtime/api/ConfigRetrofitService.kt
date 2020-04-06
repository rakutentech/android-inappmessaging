package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ConfigRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

/**
 * Retrofit API interface in order for Retrofit to make request to Config Service.
 * Contains endpoint communicating with Config Service.
 */
internal interface ConfigRetrofitService {

    /**
     * This method retrieves config.
     */
    @POST
    fun getConfigService(@Url url: String, @Body requestBody: ConfigRequest): Call<ConfigResponse>
}
