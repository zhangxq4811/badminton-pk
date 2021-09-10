package com.zxq.config;

import com.zxq.model.bo.SelfPermissionsVerify;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 自定义权限验证
 * @author zhangxianqing
 */
@Configuration
@ConfigurationProperties(prefix = "self.permissions.verify")
public class SelfPermissionsVerifyConfig {

    //必须是static修饰，不然获取不到配置的值
    private List<SelfPermissionsVerify> list;

    public List<SelfPermissionsVerify> getList() {
        return list;
    }

    public void setList(List<SelfPermissionsVerify> list) {
        this.list = list;
    }

}
