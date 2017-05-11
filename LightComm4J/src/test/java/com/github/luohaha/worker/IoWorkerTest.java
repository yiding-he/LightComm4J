package com.github.luohaha.worker;

import org.junit.Test;

/**
 * @author yiding_he
 */
public class IoWorkerTest {

    @Test
    public void testInterrupt() throws Exception {
        IoWorker ioWorker = new IoWorker();
        Thread workerThread = new Thread(ioWorker);
        workerThread.start();

        Thread.sleep(10000);
        System.out.println("Interrupting worker thread...");

        workerThread.interrupt();  // worker 线程应该正常结束
    }
}