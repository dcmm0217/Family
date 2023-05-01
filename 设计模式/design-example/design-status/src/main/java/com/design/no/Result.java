package com.design.no;

import lombok.Data;

/**
 * @author huangw
 * @date 2023/4/27 23:40
 */
@Data
public class Result {
    private String code; // 编码
    private String info; // 描述

    public Result(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
