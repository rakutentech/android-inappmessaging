package com.rakuten.tech.mobile.inappmessaging.runtime

import com.rakuten.tech.mobile.inappmessaging.runtime.utils.CommonUtil

/**
 * Contains methods related to the RMC In-App Messaging SDK.
 */
internal object RmcHelper {

    /**
     * Prefix to isolate whether IAM API call came from RMC SDK.
     */
    const val RMC_PREFIX = "rmc_"

    /**
     * Checks if app is using RMC SDK by checking the existence of its main entry point public class.
     */
    @JvmStatic
    fun isUsingRmc() = CommonUtil.hasClass("com.rakuten.tech.mobile.rmc.Rmc")
}
