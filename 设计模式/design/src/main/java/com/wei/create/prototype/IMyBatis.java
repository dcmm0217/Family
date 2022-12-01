package com.wei.create.prototype;

/**
 * 原型模式定义的接口
 */
public interface IMyBatis {

    User getUserFromDb(String username) throws CloneNotSupportedException;

    User getUser(String username) throws CloneNotSupportedException;

}
