package com.secusoft.web.core.emuns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布控任务区域或框选择 1-区域选择 2-不规则圈选 3-不规则框选
 *
 * @author chjiang
 * @date 2019/6/3
 */
public enum ViSurveyTaskEreaTypeEnum {

    AREA_TYPE_REGIONAL_SELECTION(0,"区域选择"),
    AREA_TYPE_IRREGULAR_CIRCLE_SELECTION(1,"不规则圈选"),
    AREA_TYPE_IRREGULAR_BOX_SELECTION(2,"不规则框选");

    private int code;
    private String type;

    ViSurveyTaskEreaTypeEnum(int code, String type) {
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
        if (type == 0) {
            return AREA_TYPE_REGIONAL_SELECTION.type;
        } else if (type == 1) {
            return AREA_TYPE_IRREGULAR_CIRCLE_SELECTION.type;
        } else if (type == 2) {
            return AREA_TYPE_IRREGULAR_BOX_SELECTION.type;
        }
        return null;
    }

    public static final List<Map<String, Object>> list = new ArrayList<>();
    static {
        for (ViSurveyTaskEreaTypeEnum e : ViSurveyTaskEreaTypeEnum.values()) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", e.getCode());
            map1.put("type", e.getType());
            list.add(map1);
        }
    }
}
