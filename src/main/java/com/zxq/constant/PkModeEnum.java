package com.zxq.constant;

import com.zxq.model.bo.Player;

/**
 * 比赛类型
 */
public enum PkModeEnum {

    FemaleDoubles(0, "女双"),
    MaleDoubles(1, "男双"),
    MixedDoubles(2, "混双");

    private PkModeEnum(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    /**
     * 比赛类型
     */
    private Integer value;

    /**
     * 类型描述
     */
    private String name;

    public Integer getValue() {
        return this.value;
    }

    public String getName() {
        return this.name;
    }

    public static PkModeEnum convert(Integer value) {
        for (PkModeEnum pkModeEnum : PkModeEnum.values()) {
            if (pkModeEnum.value.equals(value)) {
                return pkModeEnum;
            }
        }
        return null;
    }

    public static PkModeEnum getPkModel(Player firstPlayer, Player secondPlayer) {
        if (SexEnum.FEMALE.sex().equals(firstPlayer.getSex()) && firstPlayer.getSex().equals(secondPlayer.getSex())) {
            return FemaleDoubles;
        } else if (SexEnum.MALE.sex().equals(firstPlayer.getSex()) && firstPlayer.getSex().equals(secondPlayer.getSex())) {
            return MaleDoubles;
        } else {
            return MixedDoubles;
        }
    }
}
