package com.zxq.model.bo;

import lombok.Data;

/**
 * 被选中次数
 * @author zhangxianqing
 */
@Data
public class Occupy {

    /**
     * 已选次数
     */
    private int occupy;

    /**
     * 参赛次数加1
     */
    public void increaseOccupy() {
        this.occupy ++;
    }
}
