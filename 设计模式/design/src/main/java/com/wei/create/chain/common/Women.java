package com.wei.create.chain.common;

public class Women implements IWomen {

    /**
     * 通过一个int类型的参数来描述妇女的个人状况
     * 1--未出嫁
     * 2--出嫁
     * 3--夫死
     */

    private int type = 0;
    // 妇女的请示
    private String request = "";

    public Women() {
    }

    public Women(int type, String request) {
        this.type = type;
        this.request = request;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public String getRequest() {
        return this.request;
    }
}
