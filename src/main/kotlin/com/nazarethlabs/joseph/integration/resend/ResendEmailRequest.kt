package com.nazarethlabs.joseph.integration.resend

data class ResendEmailRequest(
    val from: String,
    val to: String,
    val subject: String,
    val html: String,
)
