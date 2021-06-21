package com.rakuten.tech.mobile.inappmessaging.runtime.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.URLUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.api.MessageMixerRetrofitService
import com.rakuten.tech.mobile.inappmessaging.runtime.data.enums.UserIdentifierType
import com.rakuten.tech.mobile.inappmessaging.runtime.data.models.UserIdentifier
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.AccountRepository
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * This class will contain SDK's utility functions such as validating Config_URL, host app info and
 * more. Methods are not static because dependency injection makes this util object available to
 * everyone and easy to use. Plus static functions are difficult to test.
 */
internal object RuntimeUtil {
    private const val DEFAULT_TIMEOUT_IN_SECONDS = 5
    private val OK_HTTP_CLIENT = OkHttpClient.Builder()
            .connectTimeout(DEFAULT_TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            .readTimeout(DEFAULT_TIMEOUT_IN_SECONDS.toLong(), TimeUnit.SECONDS)
            .build()
    private val EXECUTOR = Executors.newSingleThreadExecutor()
    private val GSON_CONVERTER_FACTORY = GsonConverterFactory.create()

    /**
     * This method returns a reference of Retrofit. Retrofit is handling API calls.
     * Adding GsonConverterFactory for parsing returned JSON. Adding OkHttp to handle the main network requests.
     */
    fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(InAppMessagingConstants.TEMPLATE_BASE_URL)
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .client(OK_HTTP_CLIENT)
                .callbackExecutor(EXECUTOR)
                .build()
    }

    /**
     * This method s a thread blocking GET request to retrieve image from server.
     * Returns null if call was failed.
     * Throws IOException if an error occur when making Get request, or converting image data
     * into bytes.
     */
    @Throws(IOException::class)
    fun getImage(imageUrl: String): Bitmap? {
        if (URLUtil.isNetworkUrl(imageUrl)) {
            val getImageCall: Call<ResponseBody> =
                    getRetrofit().create(MessageMixerRetrofitService::class.java).getImage(imageUrl)
            val imageResponse = getImageCall.execute()
            if (imageResponse.isSuccessful && imageResponse.body() != null) {
                val bytes = imageResponse.body()!!.bytes()
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        }
        return null
    }

    /**
     * This method retrieves a list of UserIdentifier objects which includes userId used for User SDK login.
     */
    fun getUserIdentifiers(accountRepo: AccountRepository = AccountRepository.instance()): MutableList<UserIdentifier> {
        val identifierList = ArrayList<UserIdentifier>()
        val userId: String = accountRepo.getUserId()
        if (userId.isNotEmpty()) {
            val user = UserIdentifier(UserIdentifierType.USER_ID, userId)
            identifierList.add(user)
        }
        val rakutenId: String = accountRepo.getRakutenId()
        if (rakutenId.isNotEmpty()) {
            val user = UserIdentifier(UserIdentifierType.R_ID, rakutenId)
            identifierList.add(user)
        }
        return identifierList
    }

    /**
     * This utility method to returns the current time in milliseconds.
     */
    fun getCurrentTimeMillis(): Long = Calendar.getInstance().timeInMillis
}
