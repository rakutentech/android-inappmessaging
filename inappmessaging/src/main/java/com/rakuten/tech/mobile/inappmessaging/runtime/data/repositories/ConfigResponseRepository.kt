package com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories

import androidx.annotation.VisibleForTesting
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.config.ConfigResponseData

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
    fun getImpressionEndpoint(): String?

    /**
     * This method returns the display permission endpoint string.
     */
    fun getDisplayPermissionEndpoint(): String

    companion object {
        private var instance: ConfigResponseRepository = ConfigResponseRepositoryImpl()

        fun instance(): ConfigResponseRepository = instance

        /**
         * Resets instance to clear config data used for testing.
         */
        @VisibleForTesting
        fun resetInstance() {
            this.instance = ConfigResponseRepositoryImpl()
        }
    }

    private class ConfigResponseRepositoryImpl : ConfigResponseRepository {
        private var configResponseData: ConfigResponseData? = null

        @Throws(IllegalArgumentException::class)
        override fun addConfigResponse(data: ConfigResponseData?) {
            requireNotNull(data)
            configResponseData = data
        }

        override fun isConfigEnabled(): Boolean = configResponseData != null && configResponseData!!.enabled

        override fun getPingEndpoint(): String =
                if (configResponseData != null) {
                    configResponseData!!.endpoints?.ping.toString()
                } else {
                    ""
                }

        override fun getImpressionEndpoint(): String? =
                if (configResponseData != null) {
                    configResponseData!!.endpoints?.impression
                } else {
                    ""
                }

        override fun getDisplayPermissionEndpoint(): String =
                if (configResponseData != null) {
                    configResponseData!!.endpoints?.displayPermission.toString()
                } else {
                    ""
                }
    }
}
