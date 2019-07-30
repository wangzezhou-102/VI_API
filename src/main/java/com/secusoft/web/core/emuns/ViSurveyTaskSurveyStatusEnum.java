package com.secusoft.web.core.emuns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布控任务布控状态 0-失败 1-成功 2-无操作
 *
 * @author chjiang
 * @date 2019/6/3
 */
public enum ViSurveyTaskSurveyStatusEnum {

    SURVEY_STATUS_FAIL(0,"失败"),
    SURVEY_STATUS_SUCCESS(1,"成功"),
    SURVEY_STATUS_NOOPS(2,"无操作");

    private int code;
    private String type;

    ViSurveyTaskSurveyStatusEnum(int code, String type) {
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
            return SURVEY_STATUS_FAIL.type;
        } else if (type == 1) {
            return SURVEY_STATUS_SUCCESS.type;
        } else if (type == 2) {
            return SURVEY_STATUS_NOOPS.type;
        }
        return null;
    }

    public static final List<Map<String, Object>> list = new ArrayList<>();
    static {
        for (ViSurveyTaskSurveyStatusEnum e : ViSurveyTaskSurveyStatusEnum.values()) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", e.getCode());
            map1.put("type", e.getType());
            list.add(map1);
        }
    }
}
