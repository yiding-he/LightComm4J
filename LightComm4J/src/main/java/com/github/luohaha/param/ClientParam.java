package com.github.luohaha.param;

import com.github.luohaha.inter.OnAccept;
import com.github.luohaha.inter.OnConnError;
import com.github.luohaha.inter.OnConnection;

public class ClientParam extends Param {

    private OnConnection onConnection;

    private OnConnError onConnError;

    public OnConnError getOnConnError() {
        return onConnError;
    }

    public void setOnConnError(OnConnError onConnError) {
        this.onConnError = onConnError;
    }

    public void setOnConnection(OnConnection onConnection) {
        this.onConnection = onConnection;
    }

    @Override
    public OnAccept getOnAccept() {
        return null;
    }

    @Override
    public OnConnection getOnConnection() {
        return this.onConnection;
    }

    @Override
    public boolean isServerParam() {
        return false;
    }

}
