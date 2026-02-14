package com.nazarethlabs.joseph.config

import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.DefaultSecurityFilterChain

class SecurityConfigTest {
    @Test
    fun `securityFilterChain should configure HttpSecurity as expected`() {
        val http = mock(HttpSecurity::class.java)
        val securityFilterChain = mock(DefaultSecurityFilterChain::class.java)

        `when`(http.csrf(any())).thenReturn(http)
        `when`(http.authorizeHttpRequests(any())).thenReturn(http)
        `when`(http.oauth2Login(any())).thenReturn(http)
        `when`(http.build()).thenReturn(securityFilterChain)

        val config = SecurityConfig()
        val result = config.securityFilterChain(http)

        verify(http).csrf(any())
        verify(http).authorizeHttpRequests(any())
        verify(http).oauth2Login(any())
        verify(http).build()

        assert(result === securityFilterChain)
    }
}
