package com.github.luohaha.worker;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.handler.IoHandler;
import com.github.luohaha.param.Param;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IoWorker implements Runnable {

    private Selector selector;

    private Context context;

    private IoHandler ioHandler;

    private BlockingQueue<JobBean> jobBeans = new LinkedBlockingQueue<>();

    public IoWorker() throws IoWorkerException {
        try {
            this.context = new Context();
            this.selector = Selector.open();
            this.ioHandler = new IoHandler(this.selector, this.context);
        } catch (IOException e) {
            throw new IoWorkerException("Error initializing IoWorker: " + e.getMessage(), e);
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                this.selector.select();

                JobBean job = jobBeans.poll();
                if (job == null) {
                    continue;
                }

                if (!initSocketChannel(job)) {
                    continue;
                }

                Set<SelectionKey> keys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    handle(key);
                    iterator.remove();
                }
            } catch (Exception e) {
                throw new IoWorkerException(e);
            }
        }
    }

    /**
     * handle read or write event
     *
     * @param key
     *
     * @throws IOException
     */
    private void handle(SelectionKey key) throws IOException {

        try (SocketChannel channel = (SocketChannel) key.channel()) {
            ContextBean bean = this.context.getContextBean(channel);

            if (key.isReadable()) {
                handleRead(channel, bean);
            } else if (key.isWritable()) {
                handleWrite(channel, bean);
            }
        }
    }

    private void handleWrite(SocketChannel channel, ContextBean bean) {
        try {
            ioHandler.writeDataToRemoteSite(channel, bean.getParam().getOnWrite());
        } catch (IOException e) {
            processOnWriteError(channel, bean, e);
        }
    }

    private void handleRead(SocketChannel channel, ContextBean bean) {
        if (bean.getParam().getOnRead() == null) {
            return;
        }

        try {
            ioHandler.readDataFromRemoteSite(channel, bean.getParam().getOnRead(), bean.getParam().getOnClose());
        } catch (IOException e) {
            processOnReadError(channel, bean, e);
        }
    }

    private void processOnWriteError(SocketChannel channel, ContextBean bean, IOException e) {
        if (bean.getParam().getOnWriteError() != null)
            bean.getParam().getOnWriteError().onWriteError(bean.getConnection(), e);
        this.context.removeByChannel(channel);
    }

    private void processOnReadError(SocketChannel channel, ContextBean bean, IOException e) {
        if (bean.getParam().getOnReadError() != null)
            bean.getParam().getOnReadError().onReadError(bean.getConnection(), e);
        this.context.removeByChannel(channel);
    }

    public void dispatch(JobBean job) throws IoWorkerException {
        try {
            this.jobBeans.put(job);
            this.selector.wakeup();
        } catch (InterruptedException e) {
            throw new IoWorkerException("Error dispatching job: " + e.getMessage(), e);
        }
    }

    private boolean initSocketChannel(JobBean jobBean) throws IOException {
        SocketChannel channel = jobBean.getChannel();
        Param param = jobBean.getParam();
        channel.configureBlocking(false);
        int ops = 0;
        if (param == null) {
            return false;
        }
        if (param.getOnRead() != null) {
            ops |= SelectionKey.OP_READ;
        }
        ops |= SelectionKey.OP_WRITE;
        channel.register(this.selector, ops);
        // new connection
        Connection connection = new Connection(this.context, channel, this.selector);
        // init context
        this.context.initContext(channel, connection, ops, param);
        // call on accept or on connection
        if (param.isServerParam()) {
            if (param.getOnAccept() != null) {
                param.getOnAccept().onAccept(connection);
            }
        } else {
            if (param.getOnConnection() != null) {
                param.getOnConnection().onConnection(connection);
            }
        }
        return true;
    }

}
