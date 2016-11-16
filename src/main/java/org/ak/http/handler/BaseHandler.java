package org.ak.http.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.ak.http.ServiceFacade;
import org.ak.http.processor.ContentType;

import java.io.*;
import java.net.URLDecoder;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.ak.http.ServiceFacade.PATH;
import static org.ak.http.ServiceFacade.SESSION_ID;

/**
 * Created by Andrew on 11.10.2016.
 */
abstract class BaseHandler implements HttpHandler {

    private static final AtomicLong SESSION_COUNTER = new AtomicLong(0);

    @Override
    public void handle(HttpExchange t) throws IOException {
        try {
            String path = t.getRequestURI().getPath();
            String sessionId = getSessionId(t);
            Map<String, String> parameters = getParameters(getQuery(t));
            parameters.put(PATH, path);
            parameters.put(SESSION_ID, sessionId);
            write(parameters, t);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            writeError(e, t);
        }
    }

    protected abstract void write(Map<String, String> parameters, HttpExchange t) throws IOException;

    void setHeaders(ContentType contentType, HttpExchange t, Map<String, String> parameters) {
        Headers h = t.getResponseHeaders();
        h.set("Content-Type", contentType.getParameter());
        h.set("Cache-Control", "no-store, no-cache, must-revalidate");
        h.set("Expires", DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT"))));
        h.set("Set-Cookie", ServiceFacade.SESSION_ID + "=" + parameters.get(ServiceFacade.SESSION_ID));
    }

    protected void out(byte[] message, HttpExchange t) throws IOException {
        try (BufferedOutputStream os = new BufferedOutputStream(t.getResponseBody())) {
            os.write(message);
        }
    }

    private void writeError(Exception e, HttpExchange t) throws IOException {
        try {
            String message = ServiceFacade.INSTANCE.get500Content(e);
            byte[] bytes = message.getBytes();
            t.sendResponseHeaders(500, bytes.length);
            out(bytes, t);
        } catch (IOException ex) {
            System.out.println(ex.getMessage()); //just a connection error. give up here.
        }
    }

    private Map<String, String> getParameters(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.length() == 0) {
            return result;
        }
        try {
            query = URLDecoder.decode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //can't happen
        }
        Arrays.stream(query.split("&")).forEach(parameter -> {
            if (parameter.length() > 0) {
                String[] parameterSplitted = parameter.split("=");
                result.put(parameterSplitted[0].trim(), parameterSplitted.length > 1 ? parameterSplitted[1].trim() : null);
            }
        });
        return result;
    }

    private String getSessionId(HttpExchange t) {
        Headers h = t.getRequestHeaders();
        if (h != null) {
            List<String> cookies = h.get("Cookie");
            if (cookies != null && cookies.size() > 0) {
                for (String cookie : cookies) {
                    if (cookie.contains(SESSION_ID) && cookie.contains("=")) {
                        int semicolon = cookie.indexOf(";");
                        if (semicolon != -1) {
                            cookie = cookie.substring(0, semicolon);
                        }
                        return cookie.split("=")[1].trim();
                    }
                }
            }
        }
        return String.valueOf(SESSION_COUNTER.getAndIncrement());
    }

    private String getQuery(HttpExchange t) {
        if (t.getRequestMethod().equalsIgnoreCase("get")) {
            return t.getRequestURI().getQuery();
        } else if (t.getRequestMethod().equalsIgnoreCase("post")) {
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(t.getRequestBody(), "UTF-8"))) {
                return bufferedReader.readLine();
            } catch (IOException e) {
                System.out.println(e.getMessage());
                e.printStackTrace(System.out);
            }
        }
        return null;
    }
}
