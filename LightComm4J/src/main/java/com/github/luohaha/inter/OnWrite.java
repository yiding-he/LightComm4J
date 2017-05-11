package com.github.luohaha.inter;

import com.github.luohaha.connection.Conn;

public interface OnWrite {
	void onWrite(Conn connection) throws Exception;
}
