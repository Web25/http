package org.web25.http.drivers

import org.web25.http.*
import org.web25.http.events.*
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder
import java.util.*
import java.util.function.Supplier

/**
 * Created by felix on 9/11/15.
 */
open class DefaultHttpRequest : HttpRequest() {

    lateinit var method: String
    private val cookies: MutableMap<String, HttpCookie> = mutableMapOf()
    private val headers: MutableMap<String, HttpHeader> = mutableMapOf()
    private var entity: ByteArray? = null
    private var response: HttpResponse? = null
    private var data: MutableMap<String, ByteArray>? = null
    private var transport = Transport.HTTP
    var manager: HttpEventManager = HttpEventManager()
    private var pipe: OutputStream? = null
    private var httpTransport: HttpTransport? = null

    var port = 80
    lateinit var path: String
    lateinit var host: String

    private val drivers: MutableList<Driver> = mutableListOf()

    private var reauth = false

    override fun method(method: String): HttpRequest {
        this.method = method
        return this
    }

    override fun cookie(name: String, value: String): HttpRequest {
        cookies.put(name, HttpCookie(name, value))
        return this
    }

    final override fun header(name: String, value: String): HttpRequest {
        headers.put(name.toLowerCase(), HttpHeader(name, value))
        return this
    }

    override fun entity(entity: ByteArray): HttpRequest {
        header("Content-Length", entity.size.toString() + "")
        if (!hasHeader("Content-Type")) {
            header("Content-Type", "text/plain")
        }
        this.entity = entity
        return this
    }

    override fun entity(entity: String): HttpRequest {
        return entity(entity.toByteArray())
    }

    override fun entity(entity: Any): HttpRequest {
        return entity(entity.toString())
    }

    override fun port(port: Int): HttpRequest {
        this.port = port
        return this
    }

    override fun path(path: String): HttpRequest {
        this.path = path
        return this
    }

    override fun host(host: String): HttpRequest {
        this.host = host
        return this
    }

    override fun method(): String {
        return method
    }

    override fun basicAuth(username: Supplier<String>, password: Supplier<String>): HttpRequest {
        Authentication.basic(username, password).authenticate(this)
        return this
    }

    override fun execute(callback: HttpResponseCallback?): HttpRequest {
        val response: HttpResponse
        try {
            val socket = transport.openSocket(host, port)
            print(socket.outputStream)
            manager.raise(HttpSentEvent(this))
            response = httpTransport!!.readResponse(socket.inputStream, pipe!!)
            response.request(this)
            manager.raise(HttpReceivedEvent(this, response))
            socket.close()
        } catch (e: IOException) {
            manager.raise(object : HttpEvent(HttpEventType.ERRORED) {

            })
            throw HttpException(this, e)
        }

        var handled = false
        if (callback != null) {
            try {
                callback.receivedResponse(response)
                handled = true
            } catch (t: Throwable) {
                t.printStackTrace()
                handled = false
            }

        }
        manager.raise(HttpHandledEvent(this, response, handled))
        this.response = response
        if (response.status().status() == StatusCode.FOUND.status()) {
            try {
                val url = URL(response.header("Location")!!.value)
                this.host = url.host
                this.port = url.port
                response.cookies().forEach { httpCookie -> cookie(httpCookie.name, httpCookie.value) }
                execute(callback)
            } catch (e: MalformedURLException) {
                throw HttpException(this, e)
            }

        } else if (!reauth && response.status().status() == StatusCode.UNAUTHORIZED.status()) {
            reauth = true
            var authentications = drivers(Authentication::class.java)
            authentications = authentications.filter({ a -> a.supports(response) })
            if (authentications.isNotEmpty()) {
                val authentication = authentications[0]
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
            }
        }
        return this
    }

    override fun transport(transport: Transport): HttpRequest {
        this.transport = transport
        return this
    }

    override fun version(version: HttpVersion): HttpRequest {
        return this
    }

    override fun print(outputStream: OutputStream): HttpRequest {
        if (httpTransport == null) {
            httpTransport = HttpTransport.version11()
        }
        httpTransport!!.write(this, outputStream)
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

    override fun basicAuth(username: () -> String, password: () -> String): HttpRequest = basicAuth(Supplier { username() }, Supplier { password() })


    override fun data(key: String, value: String): HttpRequest {
        if (data == null)
            data = HashMap<String, ByteArray>()
        data!!.put(key, value.toByteArray())
        return this
    }

    override fun eventManager(manager: HttpEventManager): HttpRequest {
        this.manager = manager
        return this
    }

    override fun event(type: HttpEventType, handler: HttpEventHandler): HttpRequest {
        this.manager.addEventHandler(type, handler)
        return this
    }

    override fun using(driver: Driver): HttpRequest {
        this.drivers.add(driver)
        return this
    }

    override fun pipe(outputStream: OutputStream): HttpRequest {
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

    override fun <T : Driver> drivers(type: Class<T>): List<T> {
        val drivers = this.drivers
                .filter { type.isAssignableFrom(it.javaClass) }
                .mapTo(ArrayList<T>()) { it as T }
        return drivers
    }

    override fun cookies(): Collection<HttpCookie> {
        return cookies.values
    }

    override fun headers(): Collection<HttpHeader> {
        return headers.values
    }

    fun isHeader(name: String): Boolean {
        return headers.containsKey(name)
    }

    override fun entityBytes(): ByteArray {
        return entity!!
    }

    override fun entityString(): String {
        return String(entity!!)
    }

    override fun checkAuth(username: String, password: String): Boolean {
        return false
    }

    override fun response(): HttpResponse {
        if (response == null)
            execute()
        return response!!
    }

    override fun use(httpTransport: HttpTransport): HttpRequest {
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

    override fun hasHeader(name: String): Boolean {
        return headers.containsKey(name.toLowerCase())
    }

    protected fun response(response: HttpResponse) {
        this.response = response
    }

    override fun hasCookie(name: String): Boolean {
        return cookies.containsKey(name)
    }

    init {
        header("Connection", "close")
        header("User-Agent", "FeMoIO HTTP/0.1")
        header("Host", host)
    }


}
