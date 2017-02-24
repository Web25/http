package org.web25.http.drivers.client

import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.auth.Authentication
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.drivers.Driver
import org.web25.http.events.*
import org.web25.http.exceptions.HttpException
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*

/**
 * Created by felix on 9/11/15.
 */
open class DefaultHttpRequest(context : HttpContext) : OutgoingHttpRequest(context) {

    override val headers: MutableMap<String, HttpHeader> = TreeMap()
    override val getParameters: MutableMap<String, Any> = TreeMap()
    override val postParameters: MutableMap<String, Any> = TreeMap()

    private val log = LoggerFactory.getLogger("HTTP")

    lateinit var method: String
    private var entity: ByteArray? = null
    private var response: HttpResponse? = null
    private var data: MutableMap<String, ByteArray>? = null
    private var transport = Transport.HTTP
    var manager: HttpEventManager = HttpEventManager()
    private var pipe: OutputStream? = null
    var httpTransport: HttpTransport = HttpTransport.version11(context)
        private set

    private var authentication: Authentication? = null

    var port = 80
    lateinit var path: String
    lateinit var host: String

    private val drivers: MutableList<Driver> = mutableListOf()

    private var reauth = false

    override fun method(method: String): OutgoingHttpRequest {
        this.method = method
        return this
    }

    final override fun header(name: String, value: String): OutgoingHttpRequest {
        headers.put(name.toLowerCase(), HttpHeader(name, value))
        return this
    }

    override fun entity(entity: ByteArray): OutgoingHttpRequest {
        header("Content-Length", entity.size.toString() + "")
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entity = entity
        return this
    }

    override fun entity(entity: String): OutgoingHttpRequest {
        return entity(entity.toByteArray())
    }

    override fun entity(entity: Any): OutgoingHttpRequest {
        return entity(entity.toString())
    }

    override fun port(port: Int): OutgoingHttpRequest {
        this.port = port
        return this
    }

    override fun path(path: String): OutgoingHttpRequest {
        this.path = path
        return this
    }

    override fun host(host: String): OutgoingHttpRequest {
        header("Host", host)
        this.host = host
        return this
    }

    override fun method(): String {
        return method
    }

    override fun execute(callback: ((HttpResponse) -> Unit)?): OutgoingHttpRequest {
        val response: HttpResponse
        try {
            val socket = transport.openSocket(host, port)
            httpTransport.write(this, socket.outputStream)
            manager.raise(HttpSentEvent(this))
            response = httpTransport.readResponse(socket.inputStream, pipe, this)
            response.request(this)
            manager.raise(HttpReceivedEvent(this, response))
            socket.close()
        } catch (e: IOException) {
            manager.raise(object : HttpEvent(HttpEventType.ERRORED) {

            })
            throw HttpException(this, e)
        }


        if (response.status().status() == StatusCode.FOUND.status()) {
            manager.raise(HttpHandledEvent(this, response, false))
            try {
                if(!response.hasHeader("Location")) {
                    throw HttpException(this, "No location header provided by remote server. Redirect not possible")
                }
                val url = URL(response.header("Location").value)
                this.host = url.host
                this.port = url.port
                response.cookies.forEach { it -> cookies[it.name] = it }
                execute(callback)
            } catch (e: MalformedURLException) {
                throw HttpException(this, e)
            }

        } else if (!reauth && response.status().status() == StatusCode.UNAUTHORIZED.status()) {
            manager.raise(HttpHandledEvent(this, response, false))
            reauth = true
            val authentications = context.findAuthentications(response)
            if (authentications.isNotEmpty()) {
                val authentication = authentications.first()
                if (authentication.isInitialized && authentication.matches(this)) {
                    authentication.authenticate(this)
                    execute(callback)
                } else if (!authentication.isInitialized) {
                    authentication.init(response)
                    authentication.authenticate(this)
                    execute(callback)
                } else if (authentication.supportsMulti()) {
                    authentication.init(response)
                    authentication.authenticate(this)
                    execute(callback)
                }
            } else {
                log.warn("No suitable authentication option found for ${response.header("WWW-Authenticate").value}")
                var handled = false
                if (callback != null) {
                    try {
                        callback(response)
                        handled = true
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        handled = false
                    }

                }
                manager.raise(HttpHandledEvent(this, response, handled))
                this.response = response
            }
        } else {
            var handled = false
            if (callback != null) {
                try {
                    callback(response)
                    handled = true
                } catch (t: Throwable) {
                    t.printStackTrace()
                    handled = false
                }

            }
            manager.raise(HttpHandledEvent(this, response, handled))
            this.response = response
        }
        return this
    }

    override fun transport(transport: Transport): OutgoingHttpRequest {
        this.transport = transport
        return this
    }

    override fun version(version: HttpVersion): OutgoingHttpRequest {
        return this
    }

    private fun writeUrlFormEncoded() {
        val stringBuilder = StringBuilder()
        val iterator = data!!.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            try {
                stringBuilder.append(URLEncoder.encode(key, "UTF-8"))
                        .append("=")
                        .append(String(bytes = data!![key]!!))
            } catch (e: UnsupportedEncodingException) {
                throw HttpException(this, e)
            }

            if (iterator.hasNext()) {
                stringBuilder.append("&")
            }
        }
        entity(stringBuilder.toString())
    }


    override fun data(key: String, value: String): OutgoingHttpRequest {
        if (data == null)
            data = HashMap<String, ByteArray>()
        data!!.put(key, value.toByteArray())
        return this
    }

    override fun using(driver: Driver): HttpRequest {
        this.drivers.add(driver)
        return this
    }

    override fun pipe(outputStream: OutputStream): OutgoingHttpRequest {
        this.pipe = outputStream
        return this
    }

    override fun prepareEntity(): HttpRequest {
        if (data != null) {
            if (hasHeader("Content-Type")) {
                val contentType = header("Content-Type").value
                if (contentType == "application/x-www-form-urlencoded") {
                    writeUrlFormEncoded()
                }
            } else {
                header("Content-Type", "application/x-www-form-urlencoded")
                writeUrlFormEncoded()
            }
        }
        return this
    }


    fun isHeader(name: String): Boolean {
        return headers.containsKey(name)
    }

    override fun entityBytes(): ByteArray {
        return this.entity ?: byteArrayOf()
    }

    override fun entityString(): String {
        return String(entityBytes())
    }


    override fun response(): HttpResponse {
        if (response == null)
            execute()
        return response!!
    }

    override fun use(httpTransport: HttpTransport): OutgoingHttpRequest {
        this.httpTransport = httpTransport
        return this
    }

    override fun transport(): Transport {
        return transport
    }

    override fun path(): String {
        return path
    }

    override fun requestLine(): String {
        return method.toUpperCase() + " " + path + " HTTP/1.1"
    }

    override fun header(name: String): HttpHeader {
        return headers[name.toLowerCase()]!!
    }

    protected fun response(response: HttpResponse) {
        this.response = response
    }

    init {
        header("Connection", "close")
        header("User-Agent", "FeMoIO HTTP/0.1")
    }

    override fun host(): String = host

}
