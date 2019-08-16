package com.secusoft.web.controller;

import com.secusoft.web.service.APIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * 过滤器
 *
 * @author wangzezhou
 * @date 2019-07-25
 */
@Controller
@CrossOrigin(value = "*", maxAge = 3600)
@Slf4j
public class FilterController {
    @Resource
    private APIService apiService;

//	public static final String INDEX_PATH =
//			System.getProperty("user.dir") + File.separator + "resources" + File.separator + "index.html";

    @RequestMapping("/bkzx")
    public String szsk() {
        return "/index.html";
    }

    @RequestMapping("/bkzx/**")
    public String szsk1() {
        return "/index.html";
    }
}