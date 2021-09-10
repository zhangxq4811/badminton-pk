package com.zxq.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 分组规则
 * @author zhangxianqing
 */
@Data
public class GroupingRule {

    /**
     * 比赛日期: yyyy-mm-dd
     */
    private String playDate;

    /**
     * 分多少组
     */
    private Integer groupingCount;

    /**
     * 比赛场次
     */
    private Integer games;

    /**
     * 是否按性别分组：true-是 false-否
     */
    private Boolean groupingBySex;

    /**
     * 参数选手集合
     */
    private List<String> players;
}
