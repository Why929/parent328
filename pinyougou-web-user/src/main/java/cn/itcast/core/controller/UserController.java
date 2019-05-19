package cn.itcast.core.controller;

import cn.itcast.common.utils.PhoneFormatCheckUtils;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.PatternSyntaxException;

@RestController
@RequestMapping("/user")
public class UserController {

//    1.注入UserService层
@Reference
UserService userService;

    @RequestMapping("/sendCode")
    public Result sendCode(String phone){
//        2.在controller层直接进行手机格式的验证
        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)){
    //            成功发送验证码
                userService.sentCode(phone);
                return new Result(true,"发送成功");
            }else {
    //            发送失败
                return new Result(false,"发送失败");
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        return new Result(false,"服务器异常");
    }

//    账户注册 提交表单
    @RequestMapping("/add")//参数一定要跟页面传递的一样 普通参数
    public Result add(@RequestBody User user,String smscode){
//判断验证码是否正确,在UserServiceImpl中,如果验证码失败,需要时时反馈,因此需要抛出一个runtimeException,所以这里需要接收一下
//              这个RuntimeException中包含什么? 怎么使用?
//              答:new Result(fasle,RuntimeException.getMessage())
        try {

            userService.add(user,smscode);
            return new Result(true,"注册成功");
        } catch (RuntimeException r) {
            r.printStackTrace();
            return new Result(false,r.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }
}











