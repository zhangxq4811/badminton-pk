package com.zxq.service;

import cn.hutool.core.util.RandomUtil;
import com.zxq.config.SelfPermissionsVerifyConfig;
import com.zxq.model.bo.SelfPermissionsVerify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限验证
 * @author zhangxianqing
 */
@Service
public class PermissionsService {

    @Autowired
    private SelfPermissionsVerifyConfig selfPermissionsVerifyConfig;

    /**
     * 随机获取一个权限验证问题
     * @return
     */
    public SelfPermissionsVerify randomOne() {
        List<SelfPermissionsVerify> list = selfPermissionsVerifyConfig.getList();
        return RandomUtil.randomEle(list);
    }

    /**
     * 权限问题验证
     * @param selfPermissionsVerify
     * @return
     */
    public boolean checkPermissionsAnswer(SelfPermissionsVerify selfPermissionsVerify) {
        SelfPermissionsVerify correctPermissionsVerify = selfPermissionsVerifyConfig.getList().stream().filter(v -> v.getQuestion().equals(selfPermissionsVerify.getQuestion())).findFirst().get();
        return correctPermissionsVerify.getAnswer().equals(selfPermissionsVerify.getAnswer());
    }
}
