package com.github.luohaha.server.test;

import com.github.luohaha.client.LightCommClient;
import com.github.luohaha.param.ClientParam;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class TestClient {

    public static void main(String[] args) throws IOException {
        AtomicInteger clientCount = new AtomicInteger(0);
        ClientParam param = new ClientParam();

        param.setOnWrite((conn) -> {
            conn.write("hello".getBytes());
        });

        param.setOnRead((conn, data) -> {
            System.out.println("Message received: " + new String(data));
            clientCount.incrementAndGet();
        });

        param.setOnReadError((conn, err) -> {
            System.out.println(err.getMessage());
        });

        param.setOnWriteError((conn, err) -> {
            System.out.println(err.getMessage());
        });
        param.setOnConnError(err -> {
            System.out.println(err.getMessage());
        });

        LightCommClient client = new LightCommClient(4);

        int count = 5000;
        for (int i = 0; i < count; i++) {
            client.connect("localhost", 8888, param);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(count + " -> " + clientCount.get());
        client.close();
    }
}
