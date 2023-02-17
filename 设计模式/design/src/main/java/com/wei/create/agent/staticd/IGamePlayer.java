package com.wei.create.agent.staticd;

/**
 * 代理模式接口
 * @author huangw
 * @since 2023-02-17
 */

public interface IGamePlayer {

    void login(String user,String password);

    void killBoss();

    void upgrade();

}
