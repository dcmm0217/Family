package com.wei.create.singleton;

public class DCLSingle {

    // volatile 保证线程之间的内存可见性，防止指令重排
    private volatile static DCLSingle dclSingle = null;

    private DCLSingle() {

    }

    public static DCLSingle getInstance() {
        // 减少加锁次数，提高效率
        if (dclSingle == null) {
            synchronized (DCLSingle.class) {
                // 保证只有一个实例
                if (dclSingle == null) {
                    dclSingle = new DCLSingle();
                }
            }
        }
        return dclSingle;
    }
}
