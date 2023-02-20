package com.wei.create.adapter;

/**
 * 使用新类来继承老登录接口的代码
 */
public class SignForThirdService extends SiginService {
    // qq登录
    public ResultMsg loginForQQ(String openId) {
        // 1、openId是默认全局唯一的，我们可以把它当成一个加长用户名
        // 2、自动生成默认密码
        // 3、注册-在原来的系统在创建一个用户
        // 4、调用原来的登录方法
        return loginForRegist(openId, null);
    }

    // 微信登录
    public ResultMsg loginForWx(String openId){
        // 前置微信登录校验，如果ok，则调用自己的登录操作
        return loginForRegist(openId,null);
    }


    public ResultMsg loginForRegist(String username, String password) {
        super.regist(username, password);
        return super.login(username, password);

    }
}
