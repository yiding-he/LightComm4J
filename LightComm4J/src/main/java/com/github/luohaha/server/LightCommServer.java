package com.github.luohaha.server;

import com.github.luohaha.param.ServerParam;
import com.github.luohaha.worker.Acceptor;
import com.github.luohaha.worker.IoWorker;

public class LightCommServer {

    private ServerParam param;

    private int ioThreadPoolSize = 1;

    public LightCommServer(ServerParam serverParam, int ioThreadPoolSize) {
        this.param = serverParam;
        this.ioThreadPoolSize = ioThreadPoolSize;
    }

    /**
     * start server
     */
    public void start() {
        Acceptor acceptor = new Acceptor(this.param);
        for (int i = 0; i < ioThreadPoolSize; i++) {
            IoWorker ioWorker = new IoWorker();
            acceptor.addIoWorker(ioWorker);
            new Thread(ioWorker).start();
        }
        new Thread(acceptor).start();
    }

}
