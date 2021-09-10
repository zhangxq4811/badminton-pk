package com.zxq.model.bo;

import lombok.Data;
import lombok.ToString;

/**
 * 选手搭配情况
 * @author zhangxianqing
 */
@ToString(callSuper = true)
@Data
public class PlayerPartner extends Occupy{

    /**
     * 选手一
     */
    private Player firstPlayer;

    /**
     * 选手二
     */
    private Player secondPlayer;

    /**
     * 比赛模式：0-女双 1-男双 2-混双
     */
    private Integer pkMode;

    public PlayerPartner() {
    }

    public PlayerPartner(Player firstPlayer, Player secondPlayer, Integer pkMode) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        this.pkMode = pkMode;
    }

    @Override
    public String toString() {
        return "occupy="+getOccupy();
    }
}
