package com.hdl.videopalyerdemo.utils;

import android.os.Bundle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetWorkUtils {

    public static void get(final String address, final String userAgent, final NetWorkCallback callback) {
        new Thread(new Runnable() {
            public void run() {
                Bundle result = new Bundle();
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    StringBuilder sb = new StringBuilder();

                    connection = (HttpURLConnection) url.openConnection();
                    if (userAgent != null && userAgent.length() > 0) {
                        connection.setRequestProperty("User-Agent", userAgent);
                    }

                    if (connection.getResponseCode() == 200) {
                        InputStream input = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        String line;
                        String nl = System.getProperty("line.separator");
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                            sb.append(nl);
                        }
                    }

                    if (callback != null) {
                        result.putString("result", sb.toString());
                        callback.onServerResponse(result);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        result.putString("result", null);
                        callback.onServerResponse(result);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    public static void post(final String address, final String data, final String contentType, final NetWorkCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bundle result = new Bundle();
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoOutput(true);
                    connection.setDoInput(true);
                    connection.setUseCaches(false);
                    connection.setRequestMethod("POST");

                    if (!YiPlusUtilities.isStringNullOrEmpty(contentType)) {
                        connection.setRequestProperty("Content-Type", contentType);
                    }

                    if (data != null && data.length() > 0) {
                        OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream(), "utf-8");
                        wr.write(data);
                        wr.flush();
                        wr.close();
                    }

                    StringBuilder sb = new StringBuilder();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream input = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "utf-8"));
                        String line;
                        String nl = System.getProperty("line.separator");
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append(nl);
                        }
                    }

                    if (callback != null) {
                        result.putString("result", sb.toString());
                        System.out.println("Yi plus successful");
                        callback.onServerResponse(result);
                    }
                } catch (Exception e) {
                    if (callback != null) {
                        result.putString("result", null);
                        e.printStackTrace();
                        System.out.println("Yi plus error  " + e.getMessage());
                        callback.onServerResponse(result);
                    }
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

}
