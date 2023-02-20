package com.wei.create.chain.userDesign;

import com.wei.create.chain.userDesign.IWomen;

public class Husband extends Handler {

    // 父亲只能处理女儿的请求
    public Husband() {
        super(Handler.HUSBAND_LEVEL_REQUEST);
    }

    @Override
    protected void response(IWomen women) {
        System.out.println("--------妻子向丈夫请示--------");
        System.out.println(women.getRequest());
        System.out.println("丈夫的回答是：同意！\n");
    }
}
