package com.vladkrava.replacefilterdemo.api

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * A demo controller which demonstrates work of [com.vladkrava.replacefilterdemo.filter.ReplaceFilter]
 *
 * @see RestController
 * @see RequestMapping
 */
@RestController
@RequestMapping("/user")
class ExampleController {

    private val log: Logger = LogManager.getLogger()


    /**
     * Endpoint to log received [RequestBody] entries
     *
     * @param request a request value
     */
    @RequestMapping("/log", method = [RequestMethod.POST])
    fun logUsername(@RequestBody request: Map<String, Any>) {

        request.forEach { entry ->
            log.info("Request entry: ${entry.key} -> ${entry.value}")
        }
    }
}