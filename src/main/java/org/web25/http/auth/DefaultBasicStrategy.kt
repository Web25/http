package org.web25.http.auth

import org.slf4j.LoggerFactory
import org.web25.http.*
import org.web25.http.drivers.DefaultBase64Driver
import java.util.function.Supplier
import java.util.regex.Pattern

/**
 * Created by felix on 6/21/16.
 */
class DefaultBasicStrategy : Authentication {

    private var username: Supplier<String>
    private var password: Supplier<String>

    private var realm: String? = null
    private val pattern = Pattern.compile("realm=\"(.*)\"")
    private var host: String? = null
    private var basePath: String? = null


    constructor(username: Supplier<String>, password: Supplier<String>) {
        this.username = username
        this.password = password
    }

    constructor(username: String, password: String) {
        this.username = Supplier{ username }
        this.password = Supplier{ password }
    }

    override val isInitialized: Boolean
        get() = realm != null

    override fun matches(request: HttpRequest): Boolean {
        return host!!.equals(request.header("Host").value, ignoreCase = true) && request.path().startsWith(basePath!!)
    }

    override fun supportsMulti(): Boolean {
        return false
    }

    override fun supportsDirect(): Boolean {
        return true
    }

    override fun init(response: HttpResponse) {
        if (response.hasHeader("WWW-Authenticate")) {
            val matcher = pattern.matcher(response.header("WWW-Authenticate")!!.value)
            if (matcher.find()) {
                this.realm = matcher.group(1)
                this.host = response.request().header("Host").value
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

    override fun authenticate(request: HttpRequest) {
        val auth = username.get() + ":" + password.get()
        val drivers = request.drivers(Base64Driver::class.java)
        val driver: Base64Driver
        if (drivers.isEmpty()) {
            driver = DefaultBase64Driver()
        } else {
            driver = drivers[0]
        }
        request.header("Authorization", "Basic " + driver.encodeToString(auth.toByteArray()))
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
