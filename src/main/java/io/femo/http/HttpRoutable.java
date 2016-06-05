package io.femo.http;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Felix Resch on 29-Apr-16.
 */
public interface HttpRoutable<T extends HttpRoutable> {

    T use(HttpMiddleware handler);

    T use(String path, HttpMiddleware handler);

    T use(HttpHandler handler);

    T use(String path, HttpHandler httpHandler);

    T use(String method, String path, HttpHandler httpHandler);

    T after(HttpMiddleware middleware);

    default T get(String path, HttpHandler httpHandler) {
        return use(Http.GET, path, httpHandler);
    }

    default T post(String path, HttpHandler httpHandler) {
        return use(Http.POST, path, httpHandler);
    }

    default T put(String path, HttpHandler httpHandler) {
        return use(Http.PUT, path, httpHandler);
    }

    default T delete(String path, HttpHandler httpHandler) {
        return use(Http.DELETE, path, httpHandler);
    }

    default T update(String path, HttpHandler httpHandler) {
        return use(Http.UPDATE, path, httpHandler);
    }

    default T patch(String path, HttpHandler httpHandler) {
        return use(Http.PATCH, path, httpHandler);
    }

    boolean matches(HttpRequest httpRequest);

    @NotNull
    static String joinPaths(String path1, String path2) {
        if(path1.endsWith("/")) {
            path1 = path1.substring(0, path1.length() - 1);
        }
        if(path2.startsWith("/")) {
            path2 = path2.substring(1);
        }
        return String.join("/", path1, path2);
    }
}
