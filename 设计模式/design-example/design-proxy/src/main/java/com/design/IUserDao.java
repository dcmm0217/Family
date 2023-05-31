package com.design;


import com.design.agent.Select;

/**
 * dao层接口
 *
 * @author huangwei
 * @date 2023-05-31
 */
public interface IUserDao {

    @Select("select userName from user where id = #{uId}")
    String queryUserInfo(String uId);
}
