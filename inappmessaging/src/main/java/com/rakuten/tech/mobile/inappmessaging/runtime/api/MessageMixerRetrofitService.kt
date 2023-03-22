package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.PingRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.PingResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Url
import retrofit2.http.Body
import retrofit2.http.Streaming
import retrofit2.http.GET

/**
 * Retrofit APIs interface in order to make requests to Message Mixer.
 * Contains endpoints communicating with MessageMixer service.
 */
internal interface MessageMixerRetrofitService {

    /**
     * This method performs ping service.
     */
    @POST
    fun performPing(
        @Header(SUBSCRIPTION_ID_HEADER) subscriptionId: String,
        @Header(ACCESS_TOKEN_HEADER) accessToken: String,
        @Header(DEVICE_ID_HEADER) deviceId: String,
        @Url url: String,
        @Body requestBody: PingRequest,
    ): Call<PingResponse>

    /**
     * Retrofit API interface in order for Retrofit to make request to check display permission.
     */
    @POST
    fun getDisplayPermissionService(
        @Header(SUBSCRIPTION_ID_HEADER) subscriptionId: String,
        @Header(ACCESS_TOKEN_HEADER) accessToken: String,
        @Url url: String,
        @Body request: DisplayPermissionRequest,
    ): Call<DisplayPermissionResponse>

    /**
     * Retrofit APIs interface in order to make report impressions to Message Mixer.
     */
    @POST
    fun reportImpression(
        @Header(SUBSCRIPTION_ID_HEADER) subscriptionId: String,
        @Header(DEVICE_ID_HEADER) deviceId: String,
        @Header(ACCESS_TOKEN_HEADER) accessToken: String,
        @Url impressionUrl: String,
        @Body impressionRequest: ImpressionRequest,
    ): Call<ResponseBody>

    // ----------------------------------- Downloading Image --------------------------------------
    /**
     * Retrofit to download image using dynamic URL from Azure Storage. Use ResponseBody only, so
     * Retrofit won't convert image into object.
     */
    @Streaming
    @GET
    fun getImage(@Url imageUrl: String): Call<ResponseBody>

    companion object {
        const val DEVICE_ID_HEADER = "device_id"
        const val ACCESS_TOKEN_HEADER = "Authorization"
        const val SUBSCRIPTION_ID_HEADER = "Subscription-Id"
    }
}
