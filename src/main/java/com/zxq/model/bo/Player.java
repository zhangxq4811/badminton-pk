package com.zxq.model.bo;

import lombok.Data;
import lombok.ToString;

/**
 * 选手信息
 * @author zhangxianqing
 */
@ToString(callSuper = true)
@Data
public class Player extends Occupy{

    /**
     * 选手姓名
     */
    private String name;

    /**
     * 选手性别：male-男 female-女
     */
    private String sex;

    public Player() {
    }

    public Player(String name, String sex) {
        this.name = name;
        this.sex = sex;
    }


}
