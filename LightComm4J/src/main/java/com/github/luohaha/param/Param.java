package com.github.luohaha.param;

import com.github.luohaha.inter.*;

public abstract class Param {

    private OnRead onRead;

    private OnReadError onReadError;

    private OnWrite onWrite;

    private OnWriteError onWriteError;

    private OnClose onClose;

    public OnReadError getOnReadError() {
        return onReadError;
    }

    public void setOnReadError(OnReadError onReadError) {
        this.onReadError = onReadError;
    }

    public OnWriteError getOnWriteError() {
        return onWriteError;
    }

    public void setOnWriteError(OnWriteError onWriteError) {
        this.onWriteError = onWriteError;
    }

    public OnRead getOnRead() {
        return onRead;
    }

    public void setOnRead(OnRead onRead) {
        this.onRead = onRead;
    }

    public OnWrite getOnWrite() {
        return onWrite;
    }

    public void setOnWrite(OnWrite onWrite) {
        this.onWrite = onWrite;
    }

    public OnClose getOnClose() {
        return onClose;
    }

    public void setOnClose(OnClose onClose) {
        this.onClose = onClose;
    }

    public abstract OnAccept getOnAccept();

    public abstract OnConnection getOnConnection();

    public abstract boolean isServerParam();
}
