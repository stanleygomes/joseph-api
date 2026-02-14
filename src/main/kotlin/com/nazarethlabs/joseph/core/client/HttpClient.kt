package com.nazarethlabs.joseph.core.client

import org.springframework.util.MultiValueMap

interface HttpClient {
    fun <T> get(
        path: String,
        responseType: Class<T>,
        pathVariables: Map<String, Any> = emptyMap(),
        queryParams: MultiValueMap<String, String> = org.springframework.util.LinkedMultiValueMap(),
    ): T?

    fun <T> post(
        path: String,
        request: Any?,
        responseType: Class<T>,
        pathVariables: Map<String, Any> = emptyMap(),
    ): T?
}
