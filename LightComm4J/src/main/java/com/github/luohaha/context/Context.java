package com.github.luohaha.context;

import com.github.luohaha.connection.Connection;
import com.github.luohaha.connection.DataBag;
import com.github.luohaha.param.Param;

import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Context {

    private Map<SocketChannel, ContextBean> channelContextMappings = new ConcurrentHashMap<>();

    public Map<SocketChannel, ContextBean> getChannelContextMappings() {
        return channelContextMappings;
    }

    public ContextBean getContextBean(SocketChannel socketChannel) {
        return this.channelContextMappings.get(socketChannel);
    }

    /**
     * init this channel's context
     *
     * @param channel    Channel to be mapped
     * @param connection as context property
     * @param ops        as context property
     * @param param      as context property
     */
    public void initContext(SocketChannel channel, Connection connection, int ops, Param param) {
        ContextBean bean = new ContextBean(connection, new ArrayDeque<>(), new DataBag(), ops, param);
        this.channelContextMappings.put(channel, bean);
    }

    /**
     * remove this channel's context
     *
     * @param channel key to be removed
     */
    public void removeByChannel(SocketChannel channel) {
        this.channelContextMappings.remove(channel);
    }
}
