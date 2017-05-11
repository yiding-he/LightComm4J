package com.github.luohaha.client;

import com.github.luohaha.param.ClientParam;
import com.github.luohaha.worker.Connector;
import com.github.luohaha.worker.IoWorker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LightCommClient {

    private Connector connector;

    private List<Thread> workerThreads = new ArrayList<>();

    private Thread connectorThread;

    public LightCommClient(int ioThreadPoolSize) throws IOException {
        startConnector();
        startWorkers(ioThreadPoolSize);
    }

    private void startConnector() {
        this.connector = new Connector();
        this.connectorThread = new Thread(connector);
        this.connectorThread.start();
    }

    private void startWorkers(int ioThreadPoolSize) {
        IntStream.range(0, ioThreadPoolSize).forEach(i -> {
            IoWorker ioWorker = new IoWorker();
            this.connector.addWorker(ioWorker);
            this.createAndRunWorkerThread(ioWorker);
        });
    }

    private void createAndRunWorkerThread(IoWorker ioWorker) {
        Thread workerThread = new Thread(ioWorker);
        workerThread.start();
        this.workerThreads.add(workerThread);
    }

    public void connect(String host, int port, ClientParam param) throws IOException {
        this.connector.connect(host, port, param);
    }

    public void close() {
        this.connectorThread.interrupt();
        this.workerThreads.forEach(Thread::interrupt);
    }
}
