package com.zxq.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.zxq.model.bo.Player;
import com.zxq.model.exception.PlayerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerService {

    private final String playerDataJsonPath = "data/player.json";

    private final List<Player> DefaultPlayers = new ArrayList<>();

    private ExecutorService writeFileThreadPool =  Executors.newSingleThreadExecutor();

    /**
     * 从文件中加载选手信息
     */
    @PostConstruct
    public void loadPlayerData() {
        String playerJson = ResourceUtil.readUtf8Str(playerDataJsonPath);
        log.info("读取player.json内容为：{}", playerJson);
        JSONArray jsonArray = JSONUtil.parseArray(playerJson);
        for (Object o : jsonArray) {
            Player player = JSONUtil.toBean(JSONUtil.toJsonStr(o), Player.class);
            DefaultPlayers.add(player);
        }
        log.info("初始化选手信息完成,DefaultPlayers：{}", DefaultPlayers);
    }

    /**
     * 获取所有选手信息
     * @return
     */
    public List<Player> allPlayers() {
        return DefaultPlayers;
    }

    /**
     * 查询选手信息
     * @param query
     * @return
     */
    public List<Player> listPlayer(Player query) {
        // 增加过滤条件
        Predicate<Player> predicate = (t) -> {
            boolean result = true;
            if (StrUtil.isNotEmpty(query.getName())) {
                result = result && t.getName().equals(query.getName());
            }
            if (StrUtil.isNotEmpty(query.getSex())) {
                result = result && t.getSex().equals(query.getSex());
            }
            return result;
        };

        List<Player> players = DefaultPlayers.stream().filter(predicate).collect(Collectors.toList());

        return players;
    }

    /**
     * 添加选手信息
     * @param player
     */
    public List<Player> addPlayer(Player player) {
        Player byName = this.findByName(player.getName());
        if (byName != null) {
            throw new PlayerException("选手已存在");
        }
        DefaultPlayers.add(player);
        // 异步将内存数据同步到本地文件
        this.asyncPlayerDataToLocalFile(DefaultPlayers);
        return DefaultPlayers;
    }

    /**
     * 异步将最新选手信息写到本地文件
     */
    private void asyncPlayerDataToLocalFile(List<Player> players) {
        CompletableFuture.runAsync(() -> this.writeJsonToFile(players), writeFileThreadPool);
    }

    /**
     * 将数据以json格式写入文件
     * @param players
     */
    private synchronized void writeJsonToFile(List<Player> players) {
        log.info("开始异步将选手数据：{}写入{}", JSONUtil.toJsonStr(players), playerDataJsonPath);
        // 自定义写入文件的JSON格式
        StringBuffer playerJsonStr = new StringBuffer();
        playerJsonStr.append("[");
        for (int i = 0; i < DefaultPlayers.size(); i++) {
            String row = JSONUtil.toJsonStr(DefaultPlayers.get(i));
            if (i != DefaultPlayers.size() - 1) {
                playerJsonStr.append("\n\t" + row + ",");
            } else {
                playerJsonStr.append("\n\t" + row + "\n");
            }
        }
        playerJsonStr.append("]");
        URL resource = ResourceUtil.getResource(playerDataJsonPath);
        FileUtil.writeUtf8String(playerJsonStr.toString(), resource.getPath());
        log.info("文件写入完成：{}", playerJsonStr);
    }


    /**
     * 根据选手名删除选手
     * @param name
     */
    public void removePlayerByName(String name) {
        DefaultPlayers.removeIf(player -> player.getName().equals(name));
        // 异步将内存数据同步到本地文件
        this.asyncPlayerDataToLocalFile(DefaultPlayers);
    }


    /**
     * 根据选手名查询选手
     * @param playerName
     * @return
     */
    public Player findByName(String playerName) {
        return DefaultPlayers.stream().filter(v -> v.getName().equals(playerName)).findAny().orElse(null);
    }

    public List<Player> convertPlayers(List<String> playerNames) {
        List<Player> players = new ArrayList<>(playerNames.size());
        for (String playerName : playerNames) {
            Player byName = this.findByName(playerName);
            Player player = new Player(byName.getName(), byName.getSex());
            players.add(player);
        }
        return players;
    }

}
