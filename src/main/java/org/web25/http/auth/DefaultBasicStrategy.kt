package org.web25.http.auth

import org.slf4j.LoggerFactory
import org.web25.http.HttpRequest
import org.web25.http.HttpResponse
import org.web25.http.client.OutgoingHttpRequest
import org.web25.http.exceptions.HttpException
import java.util.regex.Pattern

/**
 * Created by felix on 6/21/16.
 */
class DefaultBasicStrategy : Authentication {

    private var username: () -> String
    private var password: () -> String

    private var realm: String? = null
    private val pattern = Pattern.compile("realm=\"(.*)\"")
    private var host: String? = null
    private var basePath: String? = null


    constructor(username: () -> String, password: () -> String) {
        this.username = username
        this.password = password
    }

    constructor(username: String, password: String) {
        this.username = { username }
        this.password = { password }
    }

    override val isInitialized: Boolean
        get() = realm != null

    override fun matches(request: HttpRequest): Boolean {
        return host!!.equals(request.headers["Host"], ignoreCase = true) && request.path().startsWith(basePath!!)
    }

    override fun supportsMulti(): Boolean {
        return false
    }

    override fun supportsDirect(): Boolean {
        return true
    }

    override fun init(response: HttpResponse) {
        if (response.hasHeader("WWW-Authenticate")) {
            val matcher = pattern.matcher(response.headers["WWW-Authenticate"])
            if (matcher.find()) {
                this.realm = matcher.group(1)
                this.host = response.request().headers["Host"]
                this.basePath = response.request().path()
                LOGGER.debug("Found realm {} @ {}{}", this.realm, this.host, this.basePath)
            } else {
                throw HttpException(response.request(), "Did not receive a realm. No authentication possible!")
            }
        }
    }

    override fun strategy(): String {
        return "Basic"
    }

    override fun authenticate(request: OutgoingHttpRequest) {
        val auth = username() + ":" + password()
        val driver = request.context.base64()
        request.header("Authorization", "Basic " + driver.encodeToString(auth.toByteArray()))
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
