package com.github.luohaha.worker;

import com.github.luohaha.param.ServerParam;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Acceptor implements Runnable {

    private ServerSocketChannel channel;

    private Selector selector;

    private ServerParam param;

    private List<IoWorker> workers = new ArrayList<>();

    private int workerIndex = 0;

    public Acceptor(ServerParam param) throws AcceptorException {
        try {
            this.selector = Selector.open();
            this.channel = ServerSocketChannel.open();
            this.channel.configureBlocking(false);
            this.param = param;
            this.channel.socket().bind(new InetSocketAddress(param.getHost(), param.getPort()), this.param.getBacklog());
        } catch (IOException e) {
            throw new AcceptorException("Error initializing Acceptor: " + e.getMessage(), e);
        }
    }

    public void addIoWorker(IoWorker worker) {
        if (!workers.contains(worker)) {
            workers.add(worker);
        }
    }

    public void accept() throws ClosedChannelException {
        this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
        while (true) {
            try {
                this.selector.select();
                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    try {
                        handle(key);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    iterator.remove();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    private void handle(SelectionKey key) throws InterruptedException {
        if (key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            try {
                SocketChannel channel = server.accept();
                IoWorker worker = workers.get(workerIndex);
                worker.dispatch(new JobBean(channel, this.param));
                workerIndex = (workerIndex + 1) % workers.size();
            } catch (IOException e) {
                if (param.getOnAcceptError() != null) {
                    param.getOnAcceptError().onAcceptError(e);
                }
            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            accept();
        } catch (ClosedChannelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
