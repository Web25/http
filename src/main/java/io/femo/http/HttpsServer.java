package io.femo.http;

/**
 * Created by felix on 9/14/16.
 */
public interface HttpsServer extends HttpServer {

    HttpsServer keystorePass(String keystorePass);
    HttpsServer keyPass(String keyPass);
    HttpsServer keystore(String keystore);
}
