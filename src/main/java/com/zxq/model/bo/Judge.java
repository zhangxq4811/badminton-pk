package com.zxq.model.bo;

import lombok.Data;
import lombok.ToString;

/**
 * 裁判信息
 * @author zhangxianqing
 */
@ToString(callSuper = true)
@Data
public class Judge extends Occupy{

    /**
     * 裁判名
     */
    private String name;

    public Judge(String name) {
        this.name = name;
    }
}
