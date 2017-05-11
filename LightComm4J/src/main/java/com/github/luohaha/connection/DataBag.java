package com.github.luohaha.connection;

import java.nio.ByteBuffer;

public class DataBag {

    private byte[] tmpBuffer;

    private int size;

    private int pos;

    private int remainToRead = -1;

    private boolean isFinish = false;

    private static final int CHUNK_SIZE = 1024;

    public DataBag() {
        this.size = CHUNK_SIZE;
        this.pos = 0;
        tmpBuffer = new byte[this.size];
    }

    public int readFrom(ByteBuffer buffer) {
        int start = buffer.position();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        int pos = readFromBytes(data);
        buffer.position(start + pos);
        return pos;
    }

    @SuppressWarnings("ForLoopReplaceableByForEach")
    private int readFromBytes(byte[] data) {
        // if buffer's size isn't enough
        while (this.pos + data.length > this.size) {
            extendMemory();
        }
        if (remainToRead == -1) {
            // begin to get data
            if (this.pos + data.length >= 4) {
                // data's position
                int i = 0;
                for (; this.pos < 4; i++) {
                    tmpBuffer[this.pos++] = data[i];
                }
                remainToRead = byteToInteger(tmpBuffer);
                for (; i < data.length && remainToRead > 0; i++) {
                    tmpBuffer[this.pos++] = data[i];
                    remainToRead--;
                }
                if (remainToRead == 0) {
                    this.isFinish = true;
                }
                return i;
            } else {
                for (int i = 0; i < data.length; i++) {
                    tmpBuffer[this.pos++] = data[i];
                }
                return data.length;
            }
        } else {
            // continue to get data
            if (isFinish)
                return 0;
            // data's position
            int i = 0;
            for (; i < data.length && remainToRead > 0; i++) {
                tmpBuffer[this.pos++] = data[i];
                remainToRead--;
            }
            if (remainToRead == 0) {
                this.isFinish = true;
            }
            return i;
        }
    }

    public boolean isFinish() {
        return isFinish;
    }

    /**
     * get ByteBuffer from tmpBuffer, when it finish
     *
     * @return
     */
    public ByteBuffer getByteBuffer() {
        if (isFinish()) {
            int length = byteToInteger(tmpBuffer);
            ByteBuffer buffer = ByteBuffer.allocate(length);
            buffer.put(tmpBuffer, 4, length);
            return buffer;
        } else {
            return null;
        }
    }

    /**
     * get bytes from tmpBuffer, when it finish
     *
     * @return
     */
    public byte[] getBytes() {
        if (!isFinish())
            return null;
        byte[] res = new byte[this.pos - 4];
        for (int i = 0; i < res.length; i++) {
            res[i] = this.tmpBuffer[i + 4];
        }
        return res;
    }

    /**
     * byte转int，采用小端字节序
     *
     * @param data
     *
     * @return
     */
    private int byteToInteger(byte[] data) {
        return byteToIntegerFromPos(data, 0);
    }

    /**
     * byte转int，采用小端字节序，从start位置开始
     *
     * @param data
     * @param start
     *
     * @return
     */
    private int byteToIntegerFromPos(byte[] data, int start) {
        return data[start + 3] & 0xff |
                (data[start + 2] & 0xff) << 8 |
                (data[start + 1] & 0xff) << 16 |
                (data[start] & 0xff) << 24;
    }

    /**
     * extend tmpBuffer's size
     */
    private void extendMemory() {
        this.size += CHUNK_SIZE;
        byte[] newBuffer = new byte[this.size];
        System.arraycopy(tmpBuffer, 0, newBuffer, 0, this.pos);
        this.tmpBuffer = newBuffer;
    }
}
