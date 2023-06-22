package com.rakuten.tech.mobile.inappmessaging.runtime.api

import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.DisplayPermissionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.PingRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.DisplayPermissionResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ping.MessageMixerResponse
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.DEVICE_ID_HEADER
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppMessagingConstants.SUBSCRIPTION_ID_HEADER
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Header
import retrofit2.http.Url
import retrofit2.http.Body

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
    ): Call<MessageMixerResponse>

    /**
     * Retrofit API interface in order for Retrofit to make request to check display permission.
     */
    @POST
    fun getDisplayPermissionService(
        @Header(SUBSCRIPTION_ID_HEADER) subscriptionId: String,
        @Header(ACCESS_TOKEN_HEADER) accessToken: String,
        @Header(DEVICE_ID_HEADER) deviceId: String,
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

    companion object {
        const val ACCESS_TOKEN_HEADER = "Authorization"
    }
}
