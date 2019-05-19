package cn.itcast.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
//    获取登录名:被springsecurity放到了Session中了,并且绑定了当前线程
    @RequestMapping("/showName")
    public Map<String,Object> showName(){//返回Map是回显页面头像出登录用户名
//        使用SecurityContextHolder.getContext().getAuthentication().getName()拿到用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
//         返回时间:
        Date date = new Date();
//        创建hashMap,将结果put进去
        Map<String, Object> map = new HashMap<>();
        map.put("loginName",name);
        map.put("cur_time",date);
        return map;
    }
}
