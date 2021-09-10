package com.zxq.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.constant.PkModeEnum;
import com.zxq.constant.SexEnum;
import com.zxq.model.bo.*;
import com.zxq.model.exception.PlayerException;
import com.zxq.utils.ExcelUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GroupingService {

    @Autowired
    private PlayerService playerService;

    private Configuration configuration;

    /**
     * 上一次分组结果
     */
    private GroupingResult cacheGroupingResult;

    private String PK_TEMPALE_FTL = "pk-template.ftl";

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     * 分组对抗
     * @param rule
     * @return
     */
    public GroupingResult grouping(GroupingRule rule) {
        log.debug("开始分组，分组规则：{}", JSONUtil.toJsonStr(rule));
        int playerNums = rule.getPlayers().size();
        if (playerNums < 4) {
            throw new PlayerException(StrUtil.format("选手人数[{}]无法进行分组", playerNums));
        }
        if (playerNums % rule.getGroupingCount() != 0) {
            throw new PlayerException(StrUtil.format("选手人数[{}]无法分成[{}]组", playerNums, rule.getGroupingCount()));
        }
        List<Player> players = playerService.convertPlayers(rule.getPlayers());
        // 1.对选手进行分组
        List<Team> teams;
        if (rule.getGroupingBySex()) {
            teams = this.groupingTeamBySex(players, rule.getGroupingCount());
        } else {
            teams = this.groupingTeamIgnoreSex(players, rule.getGroupingCount());
        }
        // 生成每只队伍的选手所有可能搭配情况
        teams.stream().forEach(team -> {
            team.createPlayerPartners();
            String playersName = team.getPlayers().stream().map(player -> player.getName()).collect(Collectors.joining("  "));
            team.setPlayersName(playersName);
        });
        log.debug("分组后的队伍：{}", JSONUtil.toJsonStr(teams));
        // 2.根据队伍分配比赛
        List<Game> games = this.groupingGame(teams, rule.getGames(), rule.getGroupingBySex(), this.convertJudge(rule.getPlayers()));
        log.debug("分组后的比赛：{}", JSONUtil.toJsonStr(games));

        return new GroupingResult(rule, teams, games);
    }

    /**
     * 根据队伍组织比赛
     * @param teams
     * @param nums
     * @return
     */
    private List<Game> groupingGame(List<Team> teams, Integer nums, boolean groupingBySex, List<Judge> judges) {
        List<Game> games = new ArrayList<>(nums);
        for (int i = 0; i < nums; i++) {
            Team firstTeam;
            Team secondTeam;
            // 如果只有2支队伍，就指定队伍1，队伍2
            if (teams.size() == 2) {
                firstTeam = teams.get(0);
                secondTeam = teams.get(1);
            } else {
                // 有多支队伍，则采取随机策略
                firstTeam = (Team) this.randomEleByOccupy(teams, 0, null);
                secondTeam = (Team) this.randomEleByOccupy(teams, 0, CollUtil.newHashSet(firstTeam));
            }
            firstTeam.increaseOccupy();
            secondTeam.increaseOccupy();

            Game game = this.createGame(firstTeam, secondTeam, groupingBySex, judges);
            games.add(game);
        }
        return games;
    }

    private Game createGame(Team firstTeam, Team secondTeam, boolean groupingBySex, List<Judge> judges) {
        Game game = new Game();

        PlayerPartner[] playerPartners = this.randomPlayerPartnerFromTeams(firstTeam, secondTeam, groupingBySex);
        // 队伍一数据
        PlayerPartner playerPartner1 = playerPartners[0];
        game.setFirstTeamName(firstTeam.getTeamName());
        game.setFirstTeamPlayer1(playerPartner1.getFirstPlayer().getName());
        game.setFirstTeamPlayer2(playerPartner1.getSecondPlayer().getName());
        PkModeEnum pkModel1 = PkModeEnum.convert(playerPartner1.getPkMode());

        // 队伍二数据
        PlayerPartner playerPartner2 = playerPartners[1];
        game.setSecondTeamName(secondTeam.getTeamName());
        game.setSecondTeamPlayer1(playerPartner2.getFirstPlayer().getName());
        game.setSecondTeamPlayer2(playerPartner2.getSecondPlayer().getName());
        PkModeEnum pkModel2 = PkModeEnum.convert(playerPartner2.getPkMode());

        // 对战模式
        if (groupingBySex) {
            game.setPkMode(pkModel1.getName());
        } else {
            // 不区分性别，则根据2边的搭配情况决定对战类型
            if (pkModel1.equals(pkModel2)) {
                game.setPkMode(pkModel1.getName());
            } else {
                game.setPkMode(PkModeEnum.MixedDoubles.getName());
            }
        }

        // 裁判
        Judge judge = this.randomJudge(judges, game);
        game.setJudge(Optional.ofNullable(judge).map(v -> v.getName()).orElse(""));

        return game;
    }

    /**
     * 从2支队伍中挑选一组对战选手
     * @param team1
     * @param team2
     * @param groupingBySex
     * @return
     */
    private PlayerPartner[] randomPlayerPartnerFromTeams(Team team1, Team team2, boolean groupingBySex) {
        // 按照最少参数次数选取2位选手
        List<PlayerPartner> team1PlayerPartners = team1.getPlayerPartners().stream()
                .collect(Collectors.groupingBy(v -> (v.getOccupy()+v.getFirstPlayer().getOccupy()+v.getSecondPlayer().getOccupy()), TreeMap::new, Collectors.toList()))
                .firstEntry().getValue();
        // 从队伍一中选取一组选手
        PlayerPartner team1PlayerPartner = (PlayerPartner) this.randomEleByOccupy(team1PlayerPartners, 0, null);
        team1PlayerPartner.increaseOccupy();
        team1PlayerPartner.getFirstPlayer().increaseOccupy();
        team1PlayerPartner.getSecondPlayer().increaseOccupy();

        // 分组时是否区分性别
        Predicate<PlayerPartner> predicate = groupingBySex ? v -> v.getPkMode().equals(team1PlayerPartner.getPkMode()) : v -> true;
        List<PlayerPartner> team2PlayerPartners = team2.getPlayerPartners().stream().filter(predicate)
                .collect(Collectors.groupingBy(v -> (v.getOccupy()+v.getFirstPlayer().getOccupy()+v.getSecondPlayer().getOccupy()), TreeMap::new, Collectors.toList()))
                .firstEntry().getValue();

        // 从队伍二中选取一组选手
        PlayerPartner team2PlayerPartner = (PlayerPartner) this.randomEleByOccupy(team2PlayerPartners, 0, null);
        team2PlayerPartner.increaseOccupy();
        team2PlayerPartner.getFirstPlayer().increaseOccupy();
        team2PlayerPartner.getSecondPlayer().increaseOccupy();

        return new PlayerPartner[]{team1PlayerPartner, team2PlayerPartner};
    }

    /**
     * 随机选取裁判，排除掉参赛的选手
     * @param judges
     * @param game
     * @return
     */
    private Judge randomJudge(List<Judge> judges, Game game) {
        if (CollUtil.isEmpty(judges)) {
            return null;
        }
        // 排除掉正在参赛的选手
        Set<Judge> excludes = CollUtil.newHashSet();
        for (Judge judge : judges) {
            if (judge.getName().equals(game.getFirstTeamPlayer1()) || judge.getName().equals(game.getFirstTeamPlayer2())) {
                excludes.add(judge);
            }
            if (judge.getName().equals(game.getSecondTeamPlayer1()) || judge.getName().equals(game.getSecondTeamPlayer2())) {
                excludes.add(judge);
            }
        }

        Judge judge = (Judge) this.randomEleByOccupy(judges, 0, excludes);
        judge.increaseOccupy();
        return judge;
    }

    /**
     * 根据已选次数随机从集合中选取元素
     * @param list
     * @param occupy
     * @param excludes
     * @return
     */
    private Occupy randomEleByOccupy(List<? extends Occupy> list, int occupy, Set<? extends Occupy> excludes) {
        if (list.size() == 0) {
            log.error("system error! random list size = 0 ");
            throw new PlayerException("发现一个BUG");
        }
        // 获取已参赛次数相同的元素
        List<Integer> indexList = new ArrayList<>();
        log.debug("random occupy = {},list size = {},excludes size = {}", occupy, list.size(), excludes == null ? 0 : excludes.size());
        for (int i = 0; i < list.size(); i++) {
            Occupy item = list.get(i);
            if (excludes != null && excludes.contains(item)) {
                continue;
            }
            if (item.getOccupy() == occupy) {
                indexList.add(i);
            }
        }
        if (indexList.size() > 0) {
            int randomInt = RandomUtil.randomInt(indexList.size());
            return list.get(indexList.get(randomInt));
        } else {
            return this.randomEleByOccupy(list, occupy+1, excludes);
        }
    }

    /**
     * 区分性别分组
     */
    private List<Team> groupingTeamBySex(List<Player> players, int groupingCount) {
        Map<String, List<Player>> collect = players.stream().collect(Collectors.groupingBy(v -> v.getSex()));
        // 男选手集合
        List<Player> malePlayers = Optional.ofNullable(collect.get(SexEnum.MALE.sex())).orElse(new ArrayList<>());
        if (malePlayers.size() % groupingCount != 0) {
            throw new PlayerException(StrUtil.format("男选手人数[{}]无法分成[{}]组", malePlayers.size(), groupingCount));
        }
        // 女选手集合
        List<Player> femalePlayers = Optional.ofNullable(collect.get(SexEnum.FEMALE.sex())).orElse(new ArrayList<>());
        if (femalePlayers.size() % groupingCount != 0) {
            throw new PlayerException(StrUtil.format("女选手人数[{}]无法分成[{}]组", femalePlayers.size(), groupingCount));
        }
        // 每支队伍的人数
        int nums = players.size() / groupingCount;
        // 队伍数
        List<Team> teams = new ArrayList<>(groupingCount);
        for (int i = 0; i < nums; i++) {
            for (int j = 0; j < groupingCount; j++) {
                Team team;
                if (j == teams.size()) {
                    team = new Team(StrUtil.format("第{}组", j+1), new ArrayList<>(nums));
                    teams.add(team);
                } else {
                    team = teams.get(j);
                }
                // 优先从女选手集合中挑选
                Player player = this.randomRemovePlayer(femalePlayers);
                if (player == null) {
                    // 女选手被挑完，则从男选手集合中挑选
                    player = this.randomRemovePlayer(malePlayers);
                }
                team.getPlayers().add(player);
            }
        }
        return teams;
    }

    /**
     * 不区分性别分组
     */
    private List<Team> groupingTeamIgnoreSex(List<Player> players, int groupingCount) {
        // 每支队伍的人数
        int nums = players.size() / groupingCount;
        // 队伍数
        List<Team> teams = new ArrayList<>(groupingCount);
        for (int i = 0; i < groupingCount; i++) {
            // 每只队伍随机选人
            List<Player> subPlayers = new ArrayList<>();
            for (int j = 0; j < nums; j++) {
                Player randomPlayer = this.randomRemovePlayer(players);
                subPlayers.add(randomPlayer);
            }
            teams.add(new Team(StrUtil.format("第{}组", i+1), subPlayers));
        }
        return teams;
    }

    /**
     * 随机获取一位选手，并从集合中移除
     * @param players
     * @return
     */
    private Player randomRemovePlayer(List<Player> players) {
        if (CollUtil.isEmpty(players)) {
            return null;
        }
        int randomIndex = RandomUtil.randomInt(players.size());
        Player player = players.get(randomIndex);
        players.remove(randomIndex);
        return player;
    }

    private List<Judge> convertJudge(List<String> playerNames) {
        // 选手人数小于等于4位时，无法选取裁判
        if (playerNames != null && playerNames.size() <= 4) {
            return null;
        }
        List<Judge> judges = new ArrayList<>();
        for (String name : playerNames) {
            Judge judge = new Judge(name);
            judges.add(judge);
        }
        return judges;
    }

    /**
     *
     * @param groupingResult
     * @param response
     */
    public void exportPDF(GroupingResult groupingResult, HttpServletResponse response) {
        try {
            //将数据写到临时的文件excel文件中
            Template template = freeMarkerConfigurer.getConfiguration().getTemplate(PK_TEMPALE_FTL, "utf-8");
            File outFile = new File("temp_pk_result.xlsx");
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
            template.process(groupingResult, out);
            out.close();
            // 将临时excel文件转成pdf写向客户端
            ExcelUtil.excel2Pdf(outFile, response.getOutputStream());
            // 删除临时文件
            outFile.delete();
        } catch (Exception e) {
            log.error("导出结果异常：{}", e);
        }
    }

    public GroupingResult getLastGroupingResult() {
        return cacheGroupingResult;
    }

    public void saveLastGroupingResult(GroupingResult cacheGroupingResult) {
        this.cacheGroupingResult = cacheGroupingResult;
    }
}
