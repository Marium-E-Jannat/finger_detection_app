package com.ubcohci.fingerdetection.network;

import android.os.Handler;
import android.os.Looper;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class HttpClient {

    public interface ResultHandler {
        void onResult(Map<String, String> result);
        void onFailure(Exception e);
    }

    private final ExecutorService httpExecutor;
    private HttpsURLConnection conn;

    private final ResultHandler resultHandler;

    public HttpClient(ResultHandler resultHandler) {
        this.resultHandler = resultHandler;
        this.httpExecutor = Executors.newSingleThreadExecutor();
    }

    public void start(String url, String method, Map<String, String> headers, String data) throws IOException{
        // Create a connection
        conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);

        for (Map.Entry<String, String> header: headers.entrySet()) {
            conn.setRequestProperty(header.getKey(), header.getValue());
        }

        if (conn.getRequestMethod().equals("POST")) {
            // Set allowing output
            conn.setDoOutput(true);
            conn.setChunkedStreamingMode(0);
        }

        if (conn.getRequestMethod().equals("POST") && data == null) {
            throw new RuntimeException("Request body is NULL for POST!");
        } else if (resultHandler == null) {
            throw new RuntimeException("No handler for the network result!");
        }

        // Send the runnable to executed in a separate thread
        httpExecutor.execute(
                ()  -> {
                    // Sending json
                    try  (DataOutputStream os = new DataOutputStream(conn.getOutputStream())) {
                        // Write the data
                        os.writeBytes(data);
                        os.flush();

                        // Check the response code
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
                            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
                            StringBuilder out = new StringBuilder();
                            char[] buffer = new char[1024];

                            int numRead;
                            do {
                                numRead = reader.read(buffer, 0, buffer.length);
                                if (numRead > 0) {
                                    out.append(buffer);
                                } else {
                                    break;
                                }
                            } while (true);

                            Map<String, String> result = new HashMap<>();
                            result.put("data", out.toString());

                            new Handler(Looper.getMainLooper()).post(
                                    () -> resultHandler.onResult(result)
                            );
                        } else {
                            new Handler(Looper.getMainLooper()).post(
                                    () -> resultHandler.onFailure(new RuntimeException("Status code != 200"))
                            );
                        }
                    } catch (IOException e) {
                        new Handler(Looper.getMainLooper()).post(
                                () -> resultHandler.onFailure(e)
                        );
                    }
                }
        );
    }

    public void dispose() {
//        conn.disconnect();
        this.httpExecutor.shutdown();
    }
}
