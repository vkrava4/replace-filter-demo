package com.vladkrava.replacefilterdemo.filter

import com.vladkrava.replacefilterdemo.filter.ReplaceFilter.ServletInputStreamWrapper
import com.vladkrava.replacefilterdemo.utils.ReplaceRequestBody
import com.vladkrava.replacefilterdemo.utils.replaceRequestBody
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.util.StreamUtils
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

/**
 * A filter which parses and replaces some parts of [ServletRequest] body
 *
 * @see Filter
 * @see WebFilter
 * @see ServletInputStreamWrapper
 * @see ReplaceRequestWrapper
 */
@WebFilter
class ReplaceFilter : Filter {

    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        chain?.doFilter(ReplaceRequestWrapper(request as HttpServletRequest), response)
    }

    /**
     * A wrapper class of [HttpServletRequestWrapper] which is used to store and provide [ReplaceRequestBody] data class
     *
     * @see HttpServletRequestWrapper
     * @see ReplaceRequestBody
     *
     * @property request provides request information for HTTP servlets
     */
    private class ReplaceRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

        private val log: Logger = LogManager.getLogger()
        private val replaceRequestBody: ReplaceRequestBody

        init {
            replaceRequestBody = replaceRequestBody(StreamUtils.copyToByteArray(request.inputStream))

            if (log.isDebugEnabled && replaceRequestBody.isReplaced) {
                log.debug("The request body has been successfully replaced")
            }
        }

        @Throws(IOException::class)
        override fun getInputStream(): ServletInputStream {
            return ServletInputStreamWrapper(replaceRequestBody.body)
        }
    }

    /**
     * A wrapper class of [ServletInputStream] which overrides default behaviour on [ServletInputStream.isReady] to return
     * `true` at all time as we are going to read [InputStream] at least twice
     *
     * @see ServletInputStream
     */
    private class ServletInputStreamWrapper(body: ByteArray?) : ServletInputStream() {
        private val inputStream: InputStream

        init {
            inputStream = ByteArrayInputStream(body)
        }

        override fun isFinished(): Boolean {
            return try {
                inputStream.available() == 0
            } catch (e: Exception) {
                false
            }
        }

        override fun isReady(): Boolean {
            return true
        }

        override fun setReadListener(listener: ReadListener?) {
            // Do nothing
        }

        @Throws(IOException::class)
        override fun read(): Int {
            return inputStream.read()
        }
    }
}