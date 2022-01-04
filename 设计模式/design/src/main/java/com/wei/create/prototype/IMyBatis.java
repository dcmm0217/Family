package com.wei.create.prototype;

public interface IMyBatis {

    User getUserFromDb(String username) throws CloneNotSupportedException;

    User getUser(String username) throws CloneNotSupportedException;

}
