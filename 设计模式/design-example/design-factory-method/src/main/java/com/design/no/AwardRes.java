package com.design.no;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽象奖品请求返回
 *
 * @author huangwei
 * @date 2023-06-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AwardRes {

    /**
     * 编码
     */
    private String code;

    /**
     * 描述
     */
    private String info;
}
