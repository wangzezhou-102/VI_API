package com.secusoft.web.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 过滤器
 *
 * @author wangzezhou
 * @date 2019-07-25
 */
@Slf4j
@Controller
@CrossOrigin(value = "*", maxAge = 3600)
public class FilterController {

    @RequestMapping("/bkzx")
    public String szsk() {
        return "/index.html";
    }

    @RequestMapping("/bkzx/**")
    public String szsk1() { return "/index.html"; }
}