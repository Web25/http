package org.web25.http.drivers

import org.slf4j.LoggerFactory
import org.web25.http.HttpContext
import org.web25.http.drivers.server.HttpsServerThread
import org.web25.http.server.Configurator
import org.web25.http.server.HttpsServer
import java.io.FileInputStream
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory

/**
 * Created by felix on 9/14/16.
 */
class DefaultHttpsServer : DefaultHttpServer, HttpsServer {

    private var keystore: String? = null
    private var keystorePass: String? = null
    private var keyPass: String? = null

    constructor(port: Int, context: HttpContext) : super(port, true, context) {}

    constructor(server: DefaultHttpServer, context: HttpContext) : super(server.port, true, context) {
        this.httpHandlerStack = server.httpHandlerStack
    }

    constructor(configurator: Configurator, context: HttpContext) : this(configurator.getInt("port"), context)

    override fun keystorePass(keystorePass: String): HttpsServer {
        this.keystorePass = keystorePass
        return this
    }

    override fun keyPass(keyPass: String): HttpsServer {
        this.keyPass = keyPass
        return this
    }

    override fun keystore(keystore: String): HttpsServer {
        this.keystore = keystore
        return this
    }

    override fun start(): HttpsServer {
        val sslContext: SSLContext
        try {
            sslContext = SSLContext.getInstance("TLSv1.2")
            val keyStore = KeyStore.getInstance("JKS")
            val fileInputStream = FileInputStream(keystore!!)
            keyStore.load(fileInputStream, keystorePass!!.toCharArray())

            val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
            keyManagerFactory.init(keyStore, keyPass!!.toCharArray())

            val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
            trustManagerFactory.init(keyStore)

            sslContext.init(keyManagerFactory.keyManagers, trustManagerFactory.trustManagers, null)
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        } catch (e: KeyStoreException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        } catch (e: CertificateException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        } catch (e: IOException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        } catch (e: UnrecoverableKeyException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        } catch (e: KeyManagementException) {
            LOGGER.error("Could not initialize security context", e)
            return this
        }

        try {
            this.serverThread = HttpsServerThread(httpHandlerStack, sslContext, context)
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.error("Could not start secure context", e)
            return this
        } catch (e: KeyManagementException) {
            LOGGER.error("Could not start secure context", e)
            return this
        }

        super.start()
        return this
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger("HTTP")
    }
}
