package com.zxq.model.bo;

import lombok.Data;

import java.util.List;

/**
 * 分组结果
 * @author zhangxianqing
 */
@Data
public class GroupingResult {

    /**
     * 分组规则
     */
    private GroupingRule groupingRule;

    /**
     * 分组后队伍信息
     */
    private List<Team> teams;

    /**
     * 分组后比赛信息
     */
    private List<Game> games;

    public GroupingResult() {
    }

    public GroupingResult(GroupingRule groupingRule, List<Team> teams, List<Game> games) {
        this.groupingRule = groupingRule;
        this.teams = teams;
        this.games = games;
    }
}
