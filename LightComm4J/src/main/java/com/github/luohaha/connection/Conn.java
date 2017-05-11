package com.github.luohaha.connection;

import com.github.luohaha.exception.ConnectionCloseException;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;

public interface Conn {

    void write(byte[] data) throws ConnectionCloseException, ClosedChannelException;

    void close() throws IOException;

    void doClose() throws IOException;

    SocketAddress getLocalAddress() throws IOException;

    SocketAddress getRemoteAddress() throws IOException;

    void setSendBuffer(int size) throws IOException;

    void setRecvBuffer(int size) throws IOException;

    void setKeepAlive(boolean flag) throws IOException;

    void setReUseAddr(boolean flag) throws IOException;

    void setNoDelay(boolean flag) throws IOException;

    int getSendBuffer() throws IOException;

    int getRecvBuffer() throws IOException;

    boolean getKeepAlive() throws IOException;

    boolean getReUseAddr() throws IOException;

    boolean getNoDelay() throws IOException;
}
