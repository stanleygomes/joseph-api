package com.nazarethlabs.joseph.config

import com.nazarethlabs.joseph.core.client.HttpClient
import com.nazarethlabs.joseph.core.client.WebClientAdapter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Value("\${integration.brapi.base-url}")
    private lateinit var brapiBaseUrl: String

    @Value("\${integration.brapi.token}")
    private lateinit var brapiToken: String

    @Value("\${integration.resend.base-url}")
    private lateinit var resendBaseUrl: String

    @Value("\${integration.resend.api-key}")
    private lateinit var resendApiKey: String

    @Bean
    fun brapiWebClient(builder: WebClient.Builder): WebClient =
        builder
            .baseUrl(brapiBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $brapiToken")
            .build()

    @Bean
    @Qualifier("brapiHttpClient")
    fun brapiHttpClient(
        @Qualifier("brapiWebClient") webClient: WebClient,
    ): HttpClient = WebClientAdapter(webClient)

    @Bean
    fun resendWebClient(builder: WebClient.Builder): WebClient =
        builder
            .baseUrl(resendBaseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $resendApiKey")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            .build()

    @Bean
    @Qualifier("resendHttpClient")
    fun resendHttpClient(
        @Qualifier("resendWebClient") webClient: WebClient,
    ): HttpClient = WebClientAdapter(webClient)
}
