package com.rakuten.tech.mobile.inappmessaging.runtime.workmanager.workers

import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import com.google.gson.Gson
import com.rakuten.tech.mobile.inappmessaging.runtime.data.repositories.ConfigResponseRepository
import com.rakuten.tech.mobile.inappmessaging.runtime.data.requests.ImpressionRequest
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseData
import com.rakuten.tech.mobile.inappmessaging.runtime.data.responses.ConfigResponseEndpoints
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.amshove.kluent.shouldBeEqualTo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import java.net.HttpURLConnection
import java.util.concurrent.Executors

/**
 * To test workers, see https://developer.android.com/guide/background/testing/persistent/worker-impl.
 */
@RunWith(RobolectricTestRunner::class)
class ImpressionWorkerSpec {
    private val mockWebServer = MockWebServer()

    @Before
    fun setup() {
        mockWebServer.start()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `should return failure when impression endpoint is empty`() {
        val worker = buildWorker(endpoint = "")

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return failure when impression json is null`() {
        val worker = buildWorker(jsonString = null)

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return failure when impression json is empty`() {
        val worker = buildWorker(jsonString = "")

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return failure when both impression endpoint and json are invalid`() {
        val worker = buildWorker(endpoint = "", jsonString = null)

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return failure when impression json format is invalid`() {
        val worker = buildWorker(jsonString = """key: "invalid"}""")

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return retry when response=HTTP_INTERNAL_ERROR`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))
        val worker = buildWorker()

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.retry()
    }

    @Test
    fun `should return failure when response=HTTP_MULT_CHOICE`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_MULT_CHOICE))
        val worker = buildWorker()

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.failure()
    }

    @Test
    fun `should return success when response=HTTP_OK`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_OK))
        val worker = buildWorker()

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.success()
    }

    @Test
    fun `should return retry when request fails with exception`() {
        val worker = spy(buildWorker())
        `when`(
            worker.createReportImpressionCall(
                impressionEndpoint = mockWebServer.url("impression").toString(),
                impressionRequest = Gson().fromJson(
                    worker.inputData.getString(ImpressionWorker.IMPRESSION_REQUEST_KEY), ImpressionRequest::class.java,
                ),
            ),
        ).thenThrow(IllegalArgumentException())

        worker.doWork() shouldBeEqualTo ListenableWorker.Result.retry()
    }

    private fun buildWorker(
        endpoint: String = mockWebServer.url("impression").toString(),
        jsonString: String? = """
            {
                "appVersion":"0.0.1",
                "campaignId":"1234567890",
                "impressions":[
                    {
                    "impType":"IMPRESSION",
                    "timestamp":1583851442449,
                    "type":1
                    },
                ],
                "isTest":false,
                "sdkVersion":"1.6.0-SNAPSHOT",
                "userIdentifiers":[]
            }
        """.trimIndent(),
    ): ImpressionWorker {
        val worker = TestWorkerBuilder<ImpressionWorker>(
            ApplicationProvider.getApplicationContext(),
            Executors.newSingleThreadExecutor(),
            inputData = workDataOf(ImpressionWorker.IMPRESSION_REQUEST_KEY to jsonString),
        ).build()

        ConfigResponseRepository.instance().addConfigResponse(
            ConfigResponseData(
                ConfigResponseEndpoints(impression = endpoint), 100,
            ),
        )

        return worker
    }
}
