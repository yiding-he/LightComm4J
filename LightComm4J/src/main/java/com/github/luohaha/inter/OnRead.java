package com.github.luohaha.inter;

import com.github.luohaha.connection.Conn;

public interface OnRead {

    void onRead(Conn connection, byte[] data) throws Exception;
}
