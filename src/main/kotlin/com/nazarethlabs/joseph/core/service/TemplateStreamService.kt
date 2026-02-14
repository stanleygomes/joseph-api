package com.nazarethlabs.joseph.core.service

import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class TemplateStreamService {
    fun openStream(templatePath: String): InputStream = ClassPathResource(templatePath).inputStream
}
