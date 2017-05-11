package com.github.luohaha.server.test;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.server.LightCommServer;

import java.util.concurrent.atomic.AtomicInteger;

public class TestServer {

    public static void main(String[] args) {
        AtomicInteger count = new AtomicInteger(0);
        ServerParam param = new ServerParam("localhost", 8888);
        param.setBacklog(128);

        param.setOnRead((conn, data) -> {
            System.out.println("Get message: " + new String(data));
            conn.write(String.valueOf(count.incrementAndGet()).getBytes());
        });

        param.setOnClose(conn -> {
            System.out.println("Closing connection");
            conn.doClose();
        });

        param.setOnReadError((conn, err) -> {
            System.out.println(err.getMessage());
        });

        param.setOnWriteError((conn, err) -> {
            System.out.println(err.getMessage());
        });

        param.setOnAcceptError(err -> {
            System.out.println(err.getMessage());
        });

        LightCommServer server = new LightCommServer(param, 4);
        server.start();
    }
}
