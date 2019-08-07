package com.secusoft.web.core.util;

import java.lang.reflect.Field;

/**
 * @author dzp
 * @data 2019/5/28
 */
public class StringUtils {


    /**
     * 通过反射判断类的属性是否为空(包含父类)
     * @param obj
     * @return
     * @throws IllegalAccessException
     */
    public static boolean checkObjFieldIsNull(Object obj) throws IllegalAccessException {
        boolean flag = false;
        Class<?> clazz = obj.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                f.setAccessible(true);
                if (f.get(obj) != null) {
                    flag = true;
                    return flag;
                }
            }
        }
        return flag;
    }

    /**
     * 字符串空判断
     * @author ChenDong
     * @date 2019年5月28日
     * @param str
     * @return
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * 字符串非空判断
     * @author ChenDong
     * @date 2019年5月28日
     * @param str
     * @return
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
