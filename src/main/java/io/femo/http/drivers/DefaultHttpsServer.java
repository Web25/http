package io.femo.http.drivers;

import io.femo.http.HttpException;
import io.femo.http.HttpServer;
import io.femo.http.HttpsServer;
import io.femo.http.drivers.server.HttpsServerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by felix on 9/14/16.
 */
public class DefaultHttpsServer extends DefaultHttpServer implements HttpsServer {

    private static final Logger LOGGER = LoggerFactory.getLogger("HTTP");

    private String keystore;
    private String keystorePass;
    private String keyPass;

    public DefaultHttpsServer(int port) {
        super(port, true);
    }

    public DefaultHttpsServer(DefaultHttpServer server) {
        super(server.port, true);
        this.httpHandlerStack = server.httpHandlerStack;
    }

    @Override
    public HttpsServer keystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
        return this;
    }

    @Override
    public HttpsServer keyPass(String keyPass) {
        this.keyPass = keyPass;
        return this;
    }

    @Override
    public HttpsServer keystore(String keystore) {
        this.keystore = keystore;
        return this;
    }

    @Override
    public HttpsServer start() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            FileInputStream fileInputStream = new FileInputStream(keystore);
            keyStore.load(fileInputStream, keystorePass.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, keyPass.toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException |
                IOException | UnrecoverableKeyException | KeyManagementException e) {
            LOGGER.error("Could not initialize security context", e);
            return this;
        }
        try {
            this.serverThread = new HttpsServerThread(httpHandlerStack, sslContext);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            LOGGER.error("Could not start secure context", e);
            return this;
        }
        super.start();
        return this;
    }
}
