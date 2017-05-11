package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnAcceptError;
import com.github.luohaha.inter.OnConnection;

public class ServerParam extends Param {

    public static final int DEFAULT_BACKLOG = 32;

    private String host;

    private int port;

    private int backlog = DEFAULT_BACKLOG;

    private OnAccept onAccept;

    private OnAcceptError onAcceptError;

    public ServerParam(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public void setOnAccept(OnAccept onAccept) {
        this.onAccept = onAccept;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public OnAccept getOnAccept() {
        return this.onAccept;
    }

    public OnAcceptError getOnAcceptError() {
        return onAcceptError;
    }

    public void setOnAcceptError(OnAcceptError onAcceptError) {
        this.onAcceptError = onAcceptError;
    }

    @Override
    public OnConnection getOnConnection() {
        return null;
    }

    @Override
    public boolean isServerParam() {
        return true;
    }
}
