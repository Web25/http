package org.web25.http

import org.jetbrains.annotations.Contract
import org.web25.http.exceptions.UnknownStatusCodeException
import java.util.*
import kotlin.reflect.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * Created by felix on 9/10/15.
 */
class StatusCode private constructor(private var status: Int, private var statusMessage: String) {

    @Contract(pure = true)
    fun status(): Int {
        return status
    }

    fun status(status: Int) {
        this.status = status
    }

    @Contract(pure = true)
    fun statusMessage(): String {
        return statusMessage
    }

    fun statusMessage(statusMessage: String) {
        this.statusMessage = statusMessage
    }

    @Contract(pure = true)
    override fun toString(): String {
        return "StatusCode[" + status +
                " - " + statusMessage +
                ']'
    }

    @Contract(value = "null -> false", pure = true)
    override fun equals(other: Any?): Boolean {
        return other is StatusCode && other.status == status
    }

    init {
        if(!StatusCode.index.containsKey(status)) {
            StatusCode.index[status] = this
        }
    }

    companion object {

        private val index = TreeMap<Int, StatusCode>()

        /**
         * This means that the server has received the request headers, and that the client should proceed to send the request body (in the case of a request for which a body needs to be sent; for example, a POST request). If the request body is large, sending it to a server when a request has already been rejected based upon inappropriate headers is inefficient. To have a server check if the request could be accepted based on the request's headers alone, a client must send Expect: 100-continue as a header in its initial request and check if a 100 Continue status code is received in response before continuing (or receive 417 Expectation Failed and not continue).
         */
        val CONTINUE = StatusCode(100, "Continue")

        /**
         * This means the requester has asked the server to switch protocols and the server is acknowledging that it will do so.
         */
        val SWITCHING_PROTOCOLS = StatusCode(101, "Switching Protocols")

        /**
         * RFC 2518

         * As a WebDAV request may contain many sub-requests involving file operations, it may take a long time to complete the request. This code indicates that the server has received and is processing the request, but no response is available yet. This prevents the client from timing out and assuming the request was lost.
         */
        val PROCESSING = StatusCode(102, "Processing")

        /**
         * Standard response for successful HTTP requests. The actual response will depend on the request method used. In a GET request, the response will contain an entity corresponding to the requested resource. In a POST request, the response will contain an entity describing or containing the result of the action.
         */
        val OK = StatusCode(200, "OK")

        /**
         * The request has been fulfilled and resulted in a new resource being created.
         */
        val CREATED = StatusCode(201, "Created")

        /**
         * The request has been accepted for processing, but the processing has not been completed. The request might or might not eventually be acted upon, as it might be disallowed when processing actually takes place.
         */
        val ACCEPTED = StatusCode(202, "Accepted")

        /**
         * The server successfully processed the request, but is returning information that may be from another source.
         */
        val NON_AUTHORITIVE_INFORMATION = StatusCode(203, "Non-Authoritive Information")

        /**
         * The server successfully processed the request, but is not returning any content.
         */
        val NO_CONTENT = StatusCode(204, "No Content")

        /**
         * The server successfully processed the request, but is not returning any content. Unlike a 204 response, this response requires that the requester reset the document view.
         */
        val RESET_CONTENT = StatusCode(205, "Reset Content")

        /**
         * RFC 7233

         * The server is delivering only part of the resource (byte serving) due to a range header sent by the client. The range header is used by HTTP clients to enable resuming of interrupted downloads, or split a download into multiple simultaneous streams.
         */
        val PARTIAL_CONTENT = StatusCode(206, "Partial Content")

        /**
         * RFC 4918

         * The message body that follows is an XML message and can contain a number of separate response codes, depending on how many sub-requests were made.
         */
        val MULTI_STATUS = StatusCode(207, "Multi-Status")

        /**
         * RFC 5842

         * The members of a DAV binding have already been enumerated in a previous reply to this request, and are not being included again.
         */
        val ALREADY_REPORTED = StatusCode(208, "Already Reported")

        /**
         * RFC 3229

         * The server has fulfilled a request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.
         */
        val IM_USED = StatusCode(226, "IM Used")

        /**
         * Indicates multiple options for the resource that the client may follow. It, for instance, could be used to present different format options for video, list files with different extensions, or word sense disambiguation.
         */
        val MULTIPLE_CHOICES = StatusCode(300, "Multiple Choices")

        /**
         * This and all future requests should be directed to the given URI.
         */
        val MOVED_PERMANENTLY = StatusCode(301, "Moved Permanently")

        /**
         * This is an example of industry practice contradicting the standard. The HTTP/1.0 specification (RFC 1945) required the client to perform a temporary redirect (the original describing phrase was "Moved Temporarily"),[6] but popular browsers implemented 302 with the functionality of a 303 See Other. Therefore, HTTP/1.1 added status codes 303 and 307 to distinguish between the two behaviours. However, some Web applications and frameworks fallback the 302 status code as if it were the 303.
         */
        val FOUND = StatusCode(302, "Found")

        /**
         * The response to the request can be found under another URI using a GET method. When received in response to a POST (or PUT/DELETE), it should be assumed that the server has received the data and the redirect should be issued with a separate GET message.
         */
        val SEE_OTHER = StatusCode(303, "See Other")

        /**
         * RFC 7232

         * Indicates that the resource has not been modified since the version specified by the request headers If-Modified-Since or If-None-Match. This means that there is no need to retransmit the resource, since the client still has a previously-downloaded copy.
         */
        val NOT_MODIFIED = StatusCode(304, "Not Modified")

        /**
         * The requested resource is only available through a proxy, whose address is provided in the response. Many HTTP clients (such as Mozilla and Internet Explorer) do not correctly handle responses with this status code, primarily for security reasons.
         */
        val USE_PROXY = StatusCode(305, "Use Proxy")

        /**
         * No longer used. Originally meant "Subsequent requests should fallback the specified proxy.
         */
        val SWITCH_PROXY = StatusCode(306, "Switch Proxy")

        /**
         * In this case, the request should be repeated with another URI; however, future requests should still fallback the original URI. In contrast to how 302 was historically implemented, the request method is not allowed to be changed when reissuing the original request. For instance, a POST request should be repeated using another POST request.
         */
        val TEMPORARY_REDIRECT = StatusCode(307, "Temporary Redirect")

        /**
         * The request, and all future requests should be repeated using another URI. 307 and 308 (as proposed) parallel the behaviours of 302 and 301, but do not allow the HTTP method to change. So, for example, submitting a form to a permanently redirected resource may continue smoothly.
         */
        val PERMANENT_REDIRECT = StatusCode(308, "Permanent Redirect")

        /**
         * The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing).
         */
        val BAD_REQUEST = StatusCode(400, "Bad Request")

        /**
         * Similar to 403 Forbidden, but specifically for fallback when authentication is required and has failed or has not yet been provided. The response must include a WWW-Authenticate header field containing a challenge applicable to the requested resource. See Basic access authentication and Digest access authentication.
         */
        val UNAUTHORIZED = StatusCode(401, "Unauthorized")

        /**
         * Reserved for future fallback. The original intention was that this code might be used as part of some form of digital cash or micropayment scheme, but that has not happened, and this code is not usually used. YouTube uses this status if a particular IP address has made excessive requests, and requires the person to enter a CAPTCHA.
         */
        val PAYMENT_REQUIRED = StatusCode(402, "Payment Required")

        /**
         * The request was a valid request, but the server is refusing to respond to it. Unlike a 401 Unauthorized response, authenticating will make no difference.
         */
        val FORBIDDEN = StatusCode(403, "Forbidden")

        /**
         * The requested resource could not be found but may be available again in the future. Subsequent requests by the client are permissible.
         */
        val NOT_FOUND = StatusCode(404, "Not Found")

        /**
         * A request was made of a resource using a request method not supported by that resource; for example, using GET on a form which requires data to be presented via POST, or using PUT on a read-only resource.
         */
        val METHOD_NOT_ALLOWED = StatusCode(405, "Method Not Allowed")

        /**
         * The requested resource is only capable of generating content not acceptable according to the Accept headers sent in the request.
         */
        val NOT_ACCEPTABLE = StatusCode(406, "Not Acceptable")

        /**
         * RFC 7235

         * The client must first authenticate itself with the proxy.
         */
        val PROXY_AUTHENTICATION_REQUIRED = StatusCode(407, "Proxy Authentication Required")

        /**
         * The server timed out waiting for the request. According to HTTP specifications: "The client did not produce a request within the time that the server was prepared to wait. The client MAY repeat the request without modifications at any later time."
         */
        val REQUEST_TIMEOUT = StatusCode(408, "Request Timeout")

        /**
         * Indicates that the request could not be processed because of conflict in the request, such as an edit conflict in the case of multiple updates.
         */
        val CONFLICT = StatusCode(409, "Conflict")

        /**
         * Indicates that the resource requested is no longer available and will not be available again. This should be used when a resource has been intentionally removed and the resource should be purged. Upon receiving a 410 status code, the client should not request the resource again in the future. Clients such as search engines should remove the resource from their indices. Most fallback cases do not require clients and search engines to purge the resource, and a "404 Not Found" may be used instead.
         */
        val GONE = StatusCode(410, "Gone")

        /**
         * The request did not specify the length of its content, which is required by the requested resource.
         */
        val LENGTH_REQUIRED = StatusCode(411, "Length Required")

        /**
         * RFC 7232

         * The server does not meet one of the preconditions that the requester put on the request.
         */
        val PRECONDITION_FAILED = StatusCode(412, "Precondition Failed")

        /**
         * RFC 7231

         * The request is larger than the server is willing or able to process. Called "Request Entity Too Large " previously.
         */
        val PAYLOAD_TOO_LARGE = StatusCode(413, "Payload Too Large")

        /**
         * The URI provided was too long for the server to process. Often the result of too much data being encoded as a query-string of a GET request, in which case it should be converted to a POST request.
         */
        val REQUREST_URI_TOO_LONG = StatusCode(414, "Request-URI Too Long")

        /**
         * The request entity has a media type which the server or resource does not support. For example, the client uploads an image as image/svg+xml, but the server requires that images fallback a different format.
         */
        val UNSUPPORTED_MEDIA_TYPE = StatusCode(415, "Unsupported Media Type")

        /**
         * RFC 7233

         * The client has asked for a portion of the file (byte serving), but the server cannot supply that portion. For example, if the client asked for a part of the file that lies beyond the end of the file.
         */
        val REQUESTED_RANGE_NOT_SATISFIABLE = StatusCode(416, "Requested Range Not Satisfiable")

        /**
         * The server cannot meet the requirements of the Expect request-header field.
         */
        val EXPECTATION_FAILED = StatusCode(417, "Expectation Failed")

        /**
         * RFC 2324

         * This code was defined in 1998 as one of the traditional IETF April Fools' jokes, in RFC 2324, Hyper Text Coffee Pot Control Protocol, and is not expected to be implemented by actual HTTP servers. The RFC specifies this code should be returned by tea pots requested to brew coffee.
         */
        val IM_A_TEAPOT = StatusCode(418, "I'm a teapot")

        /**
         * RFC 2616

         * Not a part of the HTTP standard, 419 Authentication Timeout denotes that previously valid authentication has expired. It is used as an alternative to 401 Unauthorized in order to differentiate from otherwise authenticated clients being denied access to specific server resources.
         */
        val AUTHENTICATION_TIMEOUT = StatusCode(419, "Authentication Timeout")

        /**
         * Not part of the HTTP standard, but returned by version 1 of the Twitter Search and Trends API when the client is being rate limited. Other services may wish to implement the 429 Too Many Requests response code instead.
         */
        val ENCHANCE_YOUR_CALM = StatusCode(420, "Enhance Your Calm")

        /**
         * HTTP/2

         * The request was directed at a server that is not able to produce a response (for example because a connection reuse).
         */
        val MISDIRECTED_REQUEST = StatusCode(421, "Misdirected Request")

        /**
         * RFC 4918

         * The request was well-formed but was unable to be followed due to semantic errors.
         */
        val UNPROCESSABLE_ENTITY = StatusCode(422, "Unprocessable Entity")

        /**
         * RFC 4918

         * The resource that is being accessed is locked.
         */
        val LOCKED = StatusCode(423, "Locked")

        /**
         * The request failed due to failure of a previous request (e.g., a PROPPATCH).
         */
        val FAILED_DEPENDENCY = StatusCode(424, "Failed Dependency")

        /**
         * The client should switch to a different protocol such as TLS/1.0, given in the Upgrade header field.
         */
        val UPGRADE_REQUIRED = StatusCode(426, "Upgrade Required")

        /**
         * RFC 6585

         * The origin server requires the request to be conditional. Intended to prevent "the 'lost update' problem, where a client GETs a resource's state, modifies it, and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict."
         */
        val PRECONDITION_REQUIRED = StatusCode(428, "Precondition Required")

        /**
         * RFC 6585

         * The user has sent too many requests in a given amount of time. Intended for fallback with rate limiting schemes.
         */
        val TOO_MANY_REQUESTS = StatusCode(429, "Too Many Requests")

        /**
         * RFC 6585

         * The server is unwilling to process the request because either an individual header field, or all the header fields collectively, are too large.
         */
        val REQUEST_HEADER_FIELDS_TOO_LARGE = StatusCode(431, "Request Header Fields Too Large")

        /**
         * A generic error message, given when an unexpected condition was encountered and no more specific message is suitable.
         */
        val INTERNAL_SERVER_ERROR = StatusCode(500, "Internal Server Error")

        /**
         * The server either does not recognize the request method, or it lacks the ability to fulfill the request. Usually this implies future availability (e.g., a new feature of a web-service API).
         */
        val NOT_IMPLEMENTED = StatusCode(501, "Not Implemented")

        /**
         * The server was acting as a gateway or proxy and received an invalid response from the upstream server.
         */
        val BAD_GATEWAY = StatusCode(502, "Bad Gateway")

        /**
         * The server is currently unavailable (because it is overloaded or down for maintenance). Generally, this is a temporary state.
         */
        val SERVICE_UNAVAILABLE = StatusCode(503, "Service Unavailable")

        /**
         * The server was acting as a gateway or proxy and did not receive a timely response from the upstream server.
         */
        val GATEWAY_TIMEOUT = StatusCode(504, "Gateway Timeout")

        /**
         * The server does not support the HTTP protocol version used in the request.
         */
        val HTTP_VERSION_NOT_SUPPORTED = StatusCode(505, "HTTP Version Not Supported")

        /**
         * RFC 2295

         * Transparent content negotiation for the request results in a circular reference.
         */
        val VARIANT_ALSO_NEGOTIATES = StatusCode(506, "Variant Also Negotiates")

        /**
         * RFC 4918

         * The server is unable to store the representation needed to complete the request.
         */
        val INSUFFICIENT_STORAGE = StatusCode(507, "Insufficient Storage")

        /**
         * RFC 5842

         * The server detected an infinite loop while processing the request (sent in lieu of 208 Already Reported).
         */
        val LOOP_DETECTED = StatusCode(508, "Loop Detected")

        /**
         * RFC 2774

         * Further extensions to the request are required for the server to fulfil it.
         */
        val NOT_EXTENDED = StatusCode(510, "Not Extended")

        /**
         * RFC 2774

         * The client needs to authenticate to gain network access. Intended for fallback by intercepting proxies used to control access to the network (e.g., "captive portals" used to require agreement to Terms of Service before granting full Internet access via a Wi-Fi hotspot).
         */
        val NETWORK_AUTHENTICATION_REQUIRED = StatusCode(511, "Network Authentication Required")

        init {
            val fields = StatusCode.Companion::class.declaredMemberProperties
            fields.forEach {
                it.isAccessible = true
                if(it.returnType == StatusCode::class) {
                    val code = it.get(StatusCode.Companion) as StatusCode
                    index[code.status] == code
                }
            }
        }

        fun find(status: Int): StatusCode {
            if (index.containsKey(status)) {
                return index.get(status)!!
            }
            throw UnknownStatusCodeException(status)
        }

        fun constructFromHttpStatusLine(statusLine: String): StatusCode {
            val statusCode = statusLine.substring(statusLine.indexOf(" ") + 1, statusLine.indexOf(" ", statusLine.indexOf(" ") + 1))
            val statusMessage = statusLine.substring(statusLine.indexOf(" ", statusLine.indexOf(" ") + 1) + 1)
            return StatusCode(Integer.parseInt(statusCode), statusMessage)
        }

        fun register(statusCode: StatusCode) {
            if (!index.containsKey(statusCode.status())) {
                index.put(statusCode.status(), statusCode)
            }
        }

        fun register(code: Int, statusMessage: String) {
            register(StatusCode(code, statusMessage))
        }
    }
}
