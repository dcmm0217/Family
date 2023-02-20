package com.wei.create.adapter;

/**
 * 老系统的登录代码如下（自己的原有登录操作）
 * 为遵循开闭原则，不修改老系统的代码，开始重构代码。
 *
 * @author huangwei
 * @since 2023-02-20
 */
public class SiginService {
    /**
     * 注册方法
     *
     * @param username
     * @param password
     * @return
     */
    public ResultMsg regist(String username, String password) {
        return new ResultMsg(200, "注册成功", new Member());
    }

    /**
     * 登录方法
     *
     * @param username
     * @param password
     * @return
     */
    public ResultMsg login(String username, String password) {
        return null;
    }

}
