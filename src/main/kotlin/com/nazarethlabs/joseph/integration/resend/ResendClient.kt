package com.nazarethlabs.joseph.integration.resend

import com.nazarethlabs.joseph.core.client.HttpClient
import com.nazarethlabs.joseph.core.port.EmailProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ResendClient(
    @Qualifier("resendHttpClient")
    private val client: HttpClient,
    @Value("\${integration.resend.config.batch-size}")
    private var batchSize: Int,
    @Value("\${integration.resend.from}")
    private var emailFrom: String,
) : EmailProvider {
    private val logger = LoggerFactory.getLogger(ResendClient::class.java)

    override fun send(
        emailList: List<String>,
        subject: String,
        htmlBody: String,
    ): List<Any?> {
        val results = mutableListOf<Any?>()
        var i = 0

        while (i < emailList.size) {
            val batch = emailList.subList(i, minOf(i + batchSize, emailList.size))

            try {
                val result = sendWithParams(batch, subject, htmlBody)
                results.add(result)
            } catch (e: Exception) {
                logger.error("Error sending emails batch ([$i[ - [${i + batch.size - 1}):", e)
                results.add(mapOf("error" to e.message, "batch" to batch))
            }

            i += batchSize
        }

        return results
    }

    private fun sendWithParams(
        emailToList: List<String>,
        subject: String,
        htmlBody: String,
    ): Any? {
        val request =
            ResendEmailRequest(
                from = this.emailFrom,
                to = emailToList.joinToString(","),
                subject = subject,
                html = htmlBody,
            )

        val response = sendEmail(request)
        if (response?.message != null && response.message.contains("error", ignoreCase = true)) {
            logger.error("Error in response of sending email: ${response.message}")
            throw Exception("Error in response of sending email: ${response.message}")
        }

        return response
    }

    private fun sendEmail(request: ResendEmailRequest): ResendEmailResponse? =
        try {
            client.post(
                path = "/emails",
                request = request,
                responseType = ResendEmailResponse::class.java,
            )
        } catch (e: Exception) {
            logger.error("Failed to send email via Resend API", e)
            null
        }
}
