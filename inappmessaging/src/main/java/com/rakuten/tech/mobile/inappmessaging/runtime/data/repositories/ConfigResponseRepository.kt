package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData
import kotlin.random.Random

/**
 * Container for the response data from config service.
 */
internal interface ConfigResponseRepository {
    /**
     * This method adds a new config response to the repo.
     * Old config response will be replaced.
     */
    @Throws(IllegalArgumentException::class)
    fun addConfigResponse(data: ConfigResponseData?)

    /**
     * This method return if config is enabled.
     */
    fun isConfigEnabled(): Boolean

    /**
     * This method returns the message mixer endpoint string.
     */
    fun getPingEndpoint(): String

    /**
     * This method returns the reporting impression endpoint string.
     */
    fun getImpressionEndpoint(): String

    /**
     * This method returns the display permission endpoint string.
     */
    fun getDisplayPermissionEndpoint(): String

    companion object {
        private var instance: ConfigResponseRepository = ConfigResponseRepositoryImpl()

        @VisibleForTesting
        internal var randomizer: Random = Random

        fun instance(): ConfigResponseRepository = instance

        /**
         * Resets instance to clear config data used for testing.
         */
        @VisibleForTesting
        fun resetInstance() {
            randomizer = Random
            this.instance = ConfigResponseRepositoryImpl()
        }
    }

    private class ConfigResponseRepositoryImpl : ConfigResponseRepository {
        private var configResponseData: ConfigResponseData? = null
        private var isEnabled = false

        @Throws(IllegalArgumentException::class)
        @SuppressWarnings("MagicNumber")
        override fun addConfigResponse(data: ConfigResponseData?) {
            requireNotNull(data)
            configResponseData = data
            val rollOut = configResponseData?.rollOutPercentage ?: 0
            isEnabled = if (rollOut > 0) {
                randomizer.nextInt(1, 101) <= rollOut
            } else {
                false
            }
        }

        override fun isConfigEnabled(): Boolean = isEnabled

        override fun getPingEndpoint(): String = configResponseData?.endpoints?.ping ?: ""

        override fun getImpressionEndpoint(): String = configResponseData?.endpoints?.impression ?: ""

        override fun getDisplayPermissionEndpoint(): String = configResponseData?.endpoints?.displayPermission ?: ""
    }
}
