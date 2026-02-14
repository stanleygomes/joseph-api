package com.nazarethlabs.joseph.core.port

interface EmailProvider {
    fun send(
        emailList: List<String>,
        subject: String,
        htmlBody: String,
    ): List<Any?>
}
