package com.wei.create.adapter;

/**
 * 客户端测试代码
 */
public class SignForThirdServiceTest {
    public static void main(String[] args) {
        SignForThirdService signForThirdService = new SignForThirdService();
        // 不改变原来的代码也可以兼容新的需求，外层还可以加一层策略模式
        ResultMsg resultMsg = signForThirdService.loginForQQ("测试唯一登录");
        if (resultMsg.getCode() == 200){
            System.out.println("登录成功！");
        }else {
            System.out.println("登录失败！");
        }
    }
}
