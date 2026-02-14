package com.nazarethlabs.joseph.integration.resend

import com.nazarethlabs.joseph.core.client.HttpClient
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class ResendClientTest {
    @Test
    fun `should instantiate ResendClient`() {
        val client = mock(HttpClient::class.java)
        val batchSize = 2
        val emailFrom = "from@example.com"

        val resendClient = ResendClient(client, batchSize, emailFrom)

        assertNotNull(resendClient)
    }
}
