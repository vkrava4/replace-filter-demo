package com.vladkrava.replacefilterdemo.utils

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

val log: Logger = LogManager.getLogger()

private val replaceRegexFunctions = mapOf(
    // ${some:text} -> <$>{jndi:example}
    Regex("\\\$\\{(.*?)}") to { value: String -> value.replace("$", "<$>") }
)

/**
 * Replaces provided [ByteArray]
 *
 * @param requestBody JSON content
 * @return resulted [ReplaceRequestBody]
 */
fun replaceRequestBody(requestBody: ByteArray): ReplaceRequestBody {
    log.debug("Original requestBody=${String(requestBody)}")

    val requestJsonParser: JsonParser? = ObjectMapper().readTree(requestBody).traverse()
    val replaceMatches = mutableMapOf<String, String>()

    while (requestJsonParser?.isClosed == false) {
        val token = requestJsonParser.nextToken()
        if (token == JsonToken.VALUE_STRING || token == JsonToken.FIELD_NAME) {
            replaceMatches.putAll(contentForReplacing(requestJsonParser.valueAsString))
        }
    }

    var replacedRequestBody = String(requestBody)
    replaceMatches.forEach { match ->
        replacedRequestBody = replacedRequestBody.replace(match.key, match.value)
    }

    if (replaceMatches.isNotEmpty()) {
        log.debug("Replaced requestBody=${replacedRequestBody}")
    }

    return ReplaceRequestBody(replacedRequestBody.toByteArray(), replaceMatches.isNotEmpty())
}

/**
 * Provides a content for replacing based on the provided input
 *
 * @param value a [String] value of a field or a field name itself
 *
 * @return a [Map] where key is the original content which needs to be replaced and a value - already replaced content. May be empty
 */
fun contentForReplacing(value: String?): Map<String, String> {
    val result = mutableMapOf<String, String>()

    replaceRegexFunctions.forEach { regex ->
        regex.key.findAll(value!!).forEach { matchResult ->
            result[matchResult.value] = regex.value(matchResult.value)
        }
    }

    return result
}

/**
 * A data class which holds replace result
 *
 * @property body replace (based on  [replaceRegexFunctions]) [ByteArray] HTTP request body
 * @property isReplaced
 */
data class ReplaceRequestBody(val body: ByteArray, val isReplaced: Boolean) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReplaceRequestBody

        if (!body.contentEquals(other.body)) return false
        if (isReplaced != other.isReplaced) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body.contentHashCode()
        result = 31 * result + isReplaced.hashCode()
        return result
    }
}