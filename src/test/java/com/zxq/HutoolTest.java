package com.zxq;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.zxq.model.bo.GroupingResult;
import com.zxq.model.bo.Player;
import com.zxq.model.bo.Team;
import com.zxq.utils.ExcelUtil;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zxq
 * @date 2020/3/27 13:45
 **/
public class HutoolTest {

    public static void main(String[] args) {
        Player john = new Player("john", "male");
        john.setOccupy(2);

        Player tom = new Player("tom", "male");
        tom.setOccupy(2);

        Player andy = new Player("andy", "male");
        andy.setOccupy(1);

        ArrayList<Player> players = CollectionUtil.newArrayList(john, tom, andy);
        Player player = players.stream().collect(Collectors.maxBy(Comparator.comparing(Player::getOccupy))).get();
        System.out.println(player);
    }

    @Test
    public void md5() throws Exception {
        String json = "{\"groupingRule\":{\"playDate\":\"2021-08-30\",\"groupingCount\":2,\"games\":2,\"groupingBySex\":true,\"players\":[\"明\",\"熊本\",\"班长\",\"表妹\"]},\"teams\":[{\"occupy\":2,\"teamName\":\"第1组\",\"players\":[{\"occupy\":2,\"name\":\"班长\",\"sex\":\"female\"},{\"occupy\":2,\"name\":\"熊本\",\"sex\":\"male\"}],\"playerPartners\":[{\"occupy\":2,\"firstPlayer\":{\"occupy\":2,\"name\":\"班长\",\"sex\":\"female\"},\"secondPlayer\":{\"occupy\":2,\"name\":\"熊本\",\"sex\":\"male\"},\"pkMode\":2}]},{\"occupy\":2,\"teamName\":\"第2组\",\"players\":[{\"occupy\":2,\"name\":\"表妹\",\"sex\":\"female\"},{\"occupy\":2,\"name\":\"明\",\"sex\":\"male\"}],\"playerPartners\":[{\"occupy\":2,\"firstPlayer\":{\"occupy\":2,\"name\":\"表妹\",\"sex\":\"female\"},\"secondPlayer\":{\"occupy\":2,\"name\":\"明\",\"sex\":\"male\"},\"pkMode\":2}]}],\"games\":[{\"firstTeamName\":\"第1组\",\"firstTeamPlayer1\":\"班长\",\"firstTeamPlayer2\":\"熊本\",\"secondTeamName\":\"第2组\",\"secondTeamPlayer1\":\"表妹\",\"secondTeamPlayer2\":\"明\",\"pkMode\":\"混双\",\"judge\":\"\"},{\"firstTeamName\":\"第1组\",\"firstTeamPlayer1\":\"班长\",\"firstTeamPlayer2\":\"熊本\",\"secondTeamName\":\"第2组\",\"secondTeamPlayer1\":\"表妹\",\"secondTeamPlayer2\":\"明\",\"pkMode\":\"混双\",\"judge\":\"\"}]}";
        GroupingResult groupingResult = JSONUtil.toBean(json, GroupingResult.class);
        List<Team> teams = groupingResult.getTeams();
        teams.stream().forEach(team -> {
            String playersName = team.getPlayers().stream().map(player -> player.getName()).collect(Collectors.joining("  "));
            team.setPlayersName(playersName);
        });
        System.out.println(groupingResult);

        Configuration configuration = new Configuration(new Version("2.3.0"));
        //第二种使用绝对路径
        configuration.setDirectoryForTemplateLoading(new File("D:\\ZXQ_WORK\\workspace\\badminton-pk\\src\\main\\resources"));

        Template template = configuration.getTemplate("templates/pk-template.ftl", "utf-8");


        //输出文档路径及名称
        File outFile = new File("temp_pk_result.xlsx");

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));
        template.process(groupingResult, out);
        out.close();

        OutputStream outputStream = new FileOutputStream(new File("temp_pk_result.pdf"));
        ExcelUtil.excel2Pdf(outFile, outputStream);
    }

}
