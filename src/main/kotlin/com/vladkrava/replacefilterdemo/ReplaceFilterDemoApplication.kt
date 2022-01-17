package com.vladkrava.replacefilterdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Main application class
 *
 * @see WebMvcConfigurer
 */
@ServletComponentScan
@SpringBootApplication
class ReplaceFilterDemoApplication : WebMvcConfigurer

/**
 * Application entry-point
 *
 * @param args application JVM arguments
 */
fun main(args: Array<String>) {
    runApplication<ReplaceFilterDemoApplication>(*args)
}
