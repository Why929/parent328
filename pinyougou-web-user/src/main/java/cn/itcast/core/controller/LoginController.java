package cn.itcast.core.controller;

import cn.itcast.core.pojo.order.OrderItem;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map showName(){
        String userLoginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap<>();

        map.put("loginName",userLoginName);
        return map;

    }
}
