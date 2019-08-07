package com.secusoft.web.core.emuns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布控任务布控类型 1-人员 2-车辆 3-事件 4-物品
 *
 * @author chjiang
 * @date 2019/6/3
 */
public enum ViSurveyTaskSurveyTypeEnum {

    SURVEY_TYPE_PEOPLE(1,"人"),
    SURVEY_TYPE_CAR(2,"车"),
    SURVEY_TYPE_THING(3,"事件"),
    SURVEY_TYPE_GOODS(4,"物品");

    private int code;
    private String type;

    ViSurveyTaskSurveyTypeEnum(int code, String type) {
        this.type = type;
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public Integer getCode() {
        return code;
    }

    public static String getDescByType(int type) {
        if (type == 1) {
            return SURVEY_TYPE_PEOPLE.type;
        } else if (type == 2) {
            return SURVEY_TYPE_CAR.type;
        } else if (type == 3) {
            return SURVEY_TYPE_THING.type;
        } else if (type == 4) {
            return SURVEY_TYPE_GOODS.type;
        }
        return null;
    }

    public static final List<Map<String, Object>> list = new ArrayList<>();
    static {
        for (ViSurveyTaskSurveyTypeEnum e : ViSurveyTaskSurveyTypeEnum.values()) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", e.getCode());
            map1.put("type", e.getType());
            list.add(map1);
        }
    }
}
