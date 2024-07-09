package com.rakuten.tech.mobile.inappmessaging.runtime

import android.content.Context
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.ClassUtil
import com.rakuten.tech.mobile.inappmessaging.runtime.utils.InAppLogger

/**
 * Contains methods related to the RMC In-App Messaging SDK.
 */
internal object RmcHelper {

    /**
     * Suffix to isolate whether IAM API call came from RMC SDK.
     */
    const val RMC_SUFFIX = "-rmc"

    private const val TAG = "RmcHelper"

    /**
     * Checks if app is using RMC SDK by checking the existence of its public class.
     */
    @JvmStatic
    fun isRmcIntegrated() = ClassUtil.hasClass("com.rakuten.tech.mobile.rmc.Rmc")

    /**
     * Returns the RMC SDK version through the resource identifier.
     *
     * @return the RMC SDK version if integrated by app, otherwise null.
     */
    @SuppressWarnings("TooGenericExceptionCaught")
    @JvmStatic
    fun getRmcVersion(context: Context): String? {
        if (!isRmcIntegrated()) {
            return null
        }

        return try {
            context.getString(
                context.resources.getIdentifier(
                    "rmc_inappmessaging__version",
                    "string",
                    context.packageName,
                ),
            )
        } catch (e: Exception) {
            InAppLogger(TAG).debug(e.message)
            null
        }
    }
}
