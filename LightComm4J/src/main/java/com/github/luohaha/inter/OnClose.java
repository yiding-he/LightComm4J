package com.github.luohaha.inter;

import com.github.luohaha.connection.Conn;

public interface OnClose {

    void onClose(Conn conn) throws Exception;
}
