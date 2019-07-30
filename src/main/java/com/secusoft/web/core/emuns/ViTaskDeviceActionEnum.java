package com.secusoft.web.core.emuns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 布控设备0-停止 1-启动 2-无意义，未操作
 *
 * @author chjiang
 * @date 2019/6/3
 */
public enum ViTaskDeviceActionEnum {

    ACTION_CLOSE(0,"停止"),
    ACTION_OPEN(1,"启动"),
    ACTION_NOOPS(2,"无操作");

    private int code;
    private String type;

    ViTaskDeviceActionEnum(int code, String type) {
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
            return ACTION_CLOSE.type;
        } else if (type == 1) {
            return ACTION_OPEN.type;
        } else if (type == 2) {
            return ACTION_NOOPS.type;
        }
        return null;
    }

    public static final List<Map<String, Object>> list = new ArrayList<>();
    static {
        for (ViTaskDeviceActionEnum e : ViTaskDeviceActionEnum.values()) {
            Map<String, Object> map1 = new HashMap<>();
            map1.put("code", e.getCode());
            map1.put("type", e.getType());
            list.add(map1);
        }
    }
}
