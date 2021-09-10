package com.zxq.model.bo;

import lombok.Data;

/**
 * 比赛信息
 * @author zhangxianqing
 */
@Data
public class Game {

    /**
     * 第一只队伍
     */
    private String firstTeamName;

    /**
     * 第一只队伍的球员1
     */
    private String firstTeamPlayer1;

    /**
     * 第一只队伍的球员2
     */
    private String firstTeamPlayer2;

    /**
     * 第二只队伍
     */
    private String secondTeamName;

    /**
     * 第二只队伍的球员1
     */
    private String secondTeamPlayer1;

    /**
     * 第二只队伍球员2
     */
    private String secondTeamPlayer2;

    /**
     * 比赛模式：0-女双 1-男双 2-混双
     */
    private String pkMode;

    /**
     * 裁判
     */
    private String judge;

}
