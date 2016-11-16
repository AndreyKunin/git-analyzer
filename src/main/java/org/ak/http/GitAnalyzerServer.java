package org.ak.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.ak.util.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Andrew on 11.10.2016.
 */
public class GitAnalyzerServer {

    private static final int SERVER_THREAD_COUNT = Configuration.INSTANCE.getInt("HTTP.server.thread.count", 10);

    private HttpServer server;
    private ExecutorService executor;
    private int port;

    public void createServer(final int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.executor = Executors.newFixedThreadPool(SERVER_THREAD_COUNT);
        server.setExecutor(executor);
    }

    public void bindHandler(String context, HttpHandler handler) {
        server.createContext(context, handler);
    }

    public void startServer() {
        server.start();
        System.out.println("HTTP server started at port " + port + ".");
    }

    public void closeServer() {
        server.stop(1);
        executor.shutdownNow();
        System.out.println("HTTP server stopped.");
    }
}
