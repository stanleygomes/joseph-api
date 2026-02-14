package com.nazarethlabs.joseph.core.client

import com.nazarethlabs.joseph.core.exceptions.IntegrationException
import org.slf4j.LoggerFactory
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class WebClientAdapter(
    private val webClient: WebClient,
) : HttpClient {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun <T> executeRequest(
        method: String,
        path: String,
        responseType: Class<T>,
        pathVariables: Map<String, Any> = emptyMap(),
        queryParams: MultiValueMap<String, String>? = null,
        requestBody: Any? = null,
    ): T? {
        try {
            val requestSpec =
                webClient
                    .method(
                        org.springframework.http.HttpMethod
                            .valueOf(method),
                    ).uri { uriBuilder ->
                        var builder = uriBuilder.path(path)
                        if (queryParams != null) {
                            builder = builder.queryParams(queryParams)
                        }
                        builder.build(pathVariables)
                    }

            val responseSpec =
                if (requestBody != null) {
                    requestSpec.bodyValue(requestBody)
                } else {
                    requestSpec
                }.retrieve()
                    .onStatus(
                        { it.isError },
                        { clientResponse ->
                            clientResponse
                                .bodyToMono<Map<String, Any>>()
                                .flatMap { errorBody ->
                                    val message = errorBody["message"]?.toString() ?: "Unknown API error"
                                    Mono.error(IntegrationException("API Error: $message"))
                                }
                        },
                    )

            return responseSpec.bodyToMono(responseType).block()
        } catch (e: Exception) {
            val errorMessage = "Failed to execute $method request to path '$path'"
            logger.error(errorMessage, e)

            if (e is IntegrationException) {
                throw e
            }

            throw IntegrationException(errorMessage)
        }
    }

    override fun <T> get(
        path: String,
        responseType: Class<T>,
        pathVariables: Map<String, Any>,
        queryParams: MultiValueMap<String, String>,
    ): T? =
        executeRequest(
            method = "GET",
            path = path,
            responseType = responseType,
            pathVariables = pathVariables,
            queryParams = queryParams,
        )

    override fun <T> post(
        path: String,
        request: Any?,
        responseType: Class<T>,
        pathVariables: Map<String, Any>,
    ): T? =
        executeRequest(
            method = "POST",
            path = path,
            responseType = responseType,
            pathVariables = pathVariables,
            requestBody = request,
        )
}
