package com.zxq.controller;

import com.zxq.constant.SexEnum;
import com.zxq.model.bo.GroupingResult;
import com.zxq.model.bo.GroupingRule;
import com.zxq.model.bo.Player;
import com.zxq.model.bo.SelfPermissionsVerify;
import com.zxq.model.vo.ResultVO;
import com.zxq.service.GroupingService;
import com.zxq.service.PermissionsService;
import com.zxq.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangxianqing
 */
@Controller
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PermissionsService permissionsService;

    @Autowired
    private GroupingService groupingService;

    /**
     * 跳转到首页
     * @return
     */
    @RequestMapping("/pk")
    public ModelAndView forwardIndex(){
        // 获取选手信息
        List<Player> allPlayers = playerService.allPlayers();
        Map<String, List<Player>> collect = allPlayers.stream().collect(Collectors.groupingBy(Player::getSex));
        List<Player> malePlayers = collect.get(SexEnum.MALE.sex());
        List<Player> femalePlayers = collect.get(SexEnum.FEMALE.sex());
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("malePlayers", malePlayers);
        modelAndView.addObject("femalePlayers", femalePlayers);
        return modelAndView;
    }

    /**
     * 跳转到选手列表页面
     * @return
     */
    @RequestMapping("/player-list")
    public ModelAndView forwardPlayerList(){
        ModelAndView modelAndView = new ModelAndView("player-list");
        return modelAndView;
    }

    /**
     * 查询选手列表
     * @param query
     * @return
     */
    @RequestMapping("/listPlayer")
    @ResponseBody
    public ResultVO listPlayer(Player query) {
        List<Player> players = playerService.listPlayer(query);
        return ResultVO.successData(players);
    }

    /**
     * 添加选手
     * @param player
     * @return
     */
    @RequestMapping("/addPlayer")
    @ResponseBody
    public ResultVO addPlayer(Player player) {
        playerService.addPlayer(player);
        return ResultVO.success();
    }

    /**
     * 删除选手
     * @param player
     * @return
     */
    @RequestMapping("/removePlayer")
    @ResponseBody
    public ResultVO removePlayer(Player player) {
        playerService.removePlayerByName(player.getName());
        return ResultVO.success();
    }

    /**
     * 开始分组
     * @param rule
     * @return
     */
    @RequestMapping("/grouping")
    @ResponseBody
    public ResultVO grouping(@RequestBody GroupingRule rule) {
        GroupingResult groupingResult = groupingService.grouping(rule);
        groupingService.saveLastGroupingResult(groupingResult);
        return ResultVO.successData(groupingResult);
    }

    /**
     * 导出上一次分组的结果
     * @return
     */
    @RequestMapping("/exportResult")
    @ResponseBody
    public ResultVO exportResult(HttpServletResponse response) {
        GroupingResult lastGroupingResult = groupingService.getLastGroupingResult();
        // 将分组数据导出为PDF
        groupingService.exportPDF(lastGroupingResult, response);
        return ResultVO.success();
    }

    /**
     * 随机获取一个权限验证问题
     * @return
     */
    @RequestMapping("/queryQuestion")
    @ResponseBody
    public ResultVO queryQuestion() {
        SelfPermissionsVerify selfPermissionsVerify = permissionsService.randomOne();
        return ResultVO.successData(selfPermissionsVerify.getQuestion());
    }

    /**
     * 验证权限问题
     * @return
     */
    @RequestMapping("/checkPermissionsAnswer")
    @ResponseBody
    public ResultVO checkPermissionsAnswer(SelfPermissionsVerify selfPermissionsVerify) {
        boolean checkResult = permissionsService.checkPermissionsAnswer(selfPermissionsVerify);
        return ResultVO.successData(checkResult);
    }

}
