package com.github.luohaha.handler;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.context.Context;
import com.github.luohaha.context.ContextBean;
import com.github.luohaha.inter.OnClose;
import com.github.luohaha.inter.OnRead;
import com.github.luohaha.inter.OnWrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;

public class IoHandler {

    private static final Logger LOG = LoggerFactory.getLogger(IoHandler.class);

    private Context context;

    private Selector selector;

    private static final int BUFFER_SIZE = 1024;

    public IoHandler(Selector selector, Context context) {
        super();
        this.context = context;
        this.selector = selector;
    }

    /**
     * read data from remote site by channel
     *
     * @param channel
     * @param onRead
     * @param onClose
     *
     * @throws IOException
     */
    public void readDataFromRemoteSite(SocketChannel channel, OnRead onRead, OnClose onClose) throws IOException {
        // store current data
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        ContextBean bean = this.context.getChannelContextMappings().get(channel);
        // read from remote side
        int count = channel.read(buffer);
        if (count >= 0) {
            // set buffer's position to 0
            buffer.flip();
            while (buffer.hasRemaining()) {
                DataBag bag = bean.getReadyToRead();
                bag.readFrom(buffer);
                if (bag.isFinish()) {
                    // finish read one data bag
                    bean.getAlreadyReadData().add(bag.getBytes());
                    bean.setReadyToRead(new DataBag());
                }
            }
            // call user's custom function
            Queue<byte[]> dataQueue = bean.getAlreadyReadData();
            while (!dataQueue.isEmpty()) {
                processOnRead(onRead, bean, dataQueue);
            }
        } else {
            // read end
            closeRead(channel);
            if (onClose != null) {
                processOnClose(onClose, bean);
            }
        }
    }

    private void processOnClose(OnClose onClose, ContextBean bean) {
        try {
            onClose.onClose(bean.getConnection());
        } catch (Exception e) {
            LOG.error("Error processing OnClose: " + e.getMessage(), e);
        }
    }

    private void processOnRead(OnRead onRead, ContextBean bean, Queue<byte[]> dataQueue) {
        try {
            onRead.onRead(bean.getConnection(), dataQueue.poll());
        } catch (Exception e) {
            LOG.error("Error processing OnRead: " + e.getMessage(), e);
        }
    }

    /**
     * write data to remote site
     *
     * @param channel
     * @param onWrite
     *
     * @throws IOException
     */
    public void writeDataToRemoteSite(SocketChannel channel, OnWrite onWrite) throws IOException {
        ContextBean bean = this.context.getChannelContextMappings().get(channel);
        Connection connection = bean.getConnection();
        // call write function when user define such function and haven't call
        // it yet!
        if (onWrite != null && !connection.isOnWriteCalled()) {
            connection.setOnWriteCalled(true);
            processOnWrite(onWrite, connection);
        }

        ByteBuffer buffer = connection.getReadyToWrite().peek();
        if (buffer != null) {
            if (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            // if this buffer finish write to buffer, delete it from queue
            if (!buffer.hasRemaining()) {
                connection.getReadyToWrite().poll();
            }
        }

        // nothing to write
        if (connection.getReadyToWrite().isEmpty()) {
            closeWrite(channel);
            if (connection.isReadyToClose()) {
                connection.doClose();
            }
        }
    }

    private void processOnWrite(OnWrite onWrite, Connection connection) {
        try {
            onWrite.onWrite(connection);
        } catch (Exception e) {
            LOG.error("Error processing OnWrite: " + e.getMessage(), e);
        }
    }

    /**
     * close write event
     *
     * @param channel
     *
     * @throws ClosedChannelException
     */
    private void closeWrite(SocketChannel channel) throws ClosedChannelException {
        closeOps(channel, SelectionKey.OP_WRITE);
    }

    /**
     * close read event
     *
     * @param channel
     *
     * @throws ClosedChannelException
     */
    private void closeRead(SocketChannel channel) throws ClosedChannelException {
        closeOps(channel, SelectionKey.OP_READ);
    }

    /**
     * close some operations
     *
     * @param channel
     * @param opsToClose
     *
     * @throws ClosedChannelException
     */
    private void closeOps(SocketChannel channel, int opsToClose) throws ClosedChannelException {
        ContextBean bean = this.context.getChannelContextMappings().get(channel);
        int ops = bean.getOps();
        ops = (~opsToClose) & ops;
        bean.setOps(ops);
        channel.register(this.selector, ops);
    }
}
