package com.secusoft.web.core.emuns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布控任务是否开启 0-关闭 1-开启 2-无操作
 *
 * @author chjiang
 * @date 2019/6/3
 */
public enum ViSurveyTaskEnableEnum {

    ENABLE_CLOSE(0,"关闭"),
    ENABLE_OPEN(1,"开启"),
    ENABLE_NOOPS(2,"无操作");

    private int code;
    private String type;

    ViSurveyTaskEnableEnum(int code, String type) {
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
            return ENABLE_CLOSE.type;
        } else if (type == 1) {
            return ENABLE_OPEN.type;
        } else if (type == 2) {
            return ENABLE_NOOPS.type;
        }
        return null;
    }

    public static final List<Map<String, Object>> list = new ArrayList<>();
    static {
        for (ViSurveyTaskEnableEnum e : ViSurveyTaskEnableEnum.values()) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", e.getCode());
            map1.put("type", e.getType());
            list.add(map1);
        }
    }
}
