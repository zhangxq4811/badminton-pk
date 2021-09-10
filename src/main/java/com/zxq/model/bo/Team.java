package com.zxq.model.bo;

import com.zxq.constant.PkModeEnum;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 队伍信息
 * @author zhangxianqing
 */
@ToString(callSuper = true)
@Data
public class Team extends Occupy{

    /**
     * 队伍名称
     */
    private String teamName;

    /**
     * 本组选手
     */
    private List<Player> players;

    /**
     * 本组选手名，空格分隔
     */
    private String playersName;

    /**
     * 选手搭配情况
     */
    private List<PlayerPartner> playerPartners;

    public Team() {
    }

    public Team(String teamName, List<Player> players) {
        this.teamName = teamName;
        this.players = players;
    }

    /**
     * 生成所有所有选手的搭配情况
     * @return
     */
    public void createPlayerPartners() {
        playerPartners = new ArrayList<>();
        for (int i = 0; i < players.size() - 1; i++) {
            Player firstPlayer = players.get(i);
            for (int j = i+1; j < players.size(); j++) {
                Player secondPlayer = players.get(j);
                Integer pkMode = PkModeEnum.getPkModel(firstPlayer, secondPlayer).getValue();
                playerPartners.add(new PlayerPartner(firstPlayer, secondPlayer, pkMode));
            }
        }
    }
}
