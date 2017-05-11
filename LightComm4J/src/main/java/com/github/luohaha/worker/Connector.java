package com.github.luohaha.worker;

import com.github.luohaha.param.ClientParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Connector implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Connector.class);

    private Selector selector;

    private List<IoWorker> workers = new ArrayList<>();

    private int workersIndex = 0;

    private ConcurrentMap<SocketChannel, ClientParam> channelParamMappings = new ConcurrentHashMap<>();

    private BlockingQueue<SocketChannel> chanQueue = new LinkedBlockingQueue<>();

    public Connector() throws ConnectorException {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new ConnectorException("Error initializing Connector: " + e.getMessage(), e);
        }
    }

    public void connect(String host, int port, ClientParam param) {
        try {
            SocketChannel socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            this.channelParamMappings.put(socketChannel, param);
            this.chanQueue.add(socketChannel);

            SocketAddress address = new InetSocketAddress(host, port);
            socketChannel.connect(address);

            this.selector.wakeup();
        } catch (IOException e) {
            throw new ConnectorException("Error connecting to "
                    + host + ":" + port + " - " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.selector.select();

                SocketChannel newChan = this.chanQueue.poll();
                if (newChan == null) {
                    continue;
                } else {
                    newChan.register(selector, SelectionKey.OP_CONNECT);
                }

                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    handle(key);
                    iterator.remove();
                }
            } catch (Exception e) {
                LOG.error("", e);
            }
        }
    }

    private void handle(SelectionKey key) throws IOException {

        try (SocketChannel channel = (SocketChannel) key.channel()) {

            if (key.isConnectable()) {
                try {
                    if (channel.finishConnect()) {
                        ClientParam clientParam = this.channelParamMappings.get(channel);
                        IoWorker worker = getNextWorker();
                        worker.dispatch(new JobBean(channel, clientParam));
                    }
                } catch (IOException e) {
                    processOnConnError(channel, e);
                    this.channelParamMappings.remove(channel);
                }
            }
        }
    }

    private IoWorker getNextWorker() {
        workersIndex = (workersIndex + 1) % workers.size();
        return workers.get(workersIndex);
    }

    private void processOnConnError(SocketChannel channel, IOException e) {
        ClientParam clientParam = this.channelParamMappings.get(channel);
        if (clientParam != null && clientParam.getOnConnError() != null) {
            clientParam.getOnConnError().onConnError(e);
        }
    }

    public void addWorker(IoWorker worker) {
        if (!this.workers.contains(worker)) {
            this.workers.add(worker);
        }
    }
}
