package cn.itcast.core.service;


import cn.itcast.core.mapper.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    JmsTemplate jmsTemplate;//配置xml:工厂一 工厂二 topic/queue Template
    @Autowired
    Destination registerCode;//发布订阅模式:会话承载名称
    @Autowired
    UserDao userDao;
// 点击发送验证码
    @Override
    public void sentCode(String phoneNum) {
//        1.获取到电话号后:1.放到缓存中一份 2.发送JMS中一份

//        2.存到缓存中一份,设置存活时间5分钟
//         生成六位验证码
        String s = RandomStringUtils.randomNumeric(6);
        redisTemplate.boundValueOps(phoneNum).set(s);
        redisTemplate.boundValueOps(phoneNum).expire(5, TimeUnit.MINUTES);
//        3.向JMS发送消息
        jmsTemplate.send(registerCode, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("PhoneNumbers",phoneNum);//电话号
                mapMessage.setString("SignName","whydiary");//短信签名
                mapMessage.setString("TemplateCode","SMS_164150804");//短信模板
                mapMessage.setString("TemplateParam","{\"code\":\""+ s+"\"}");//验证码
                return mapMessage;
            }
        });
    }

//    点击注册 提交信息表单
    @Override
    public void add(User user, String smsCode) {

//        1.判断用户输入的验证码是否正确
        String redisSmsCode = (String) redisTemplate.boundValueOps(user.getPhone()).get();
//          要点:首先用null判空
        if ( null != smsCode && redisSmsCode.equals(smsCode)){
//        2.如果验证码相同 存入user到数据库,如
            user.setCreated(new Date());//配置创建时间
            user.setUpdated(new Date());//配置更新时间
            userDao.insertSelective(user);//选择性插入
        }else {
//        3.要点:要是验证码不成功如何 实时反馈用户? 抛runtimeException,
//                  要点,就是UserController层接收到这个异常,是怎么接收的? UserContrller层引入这个实例,调用这个实例的方法,
//                  执行到else的时候,就会抛出这个异常,然后UserController层就会接收到这个异常
//            因此UserController需要进行trycatch来捕获这种异常
            throw new RuntimeException("验证码输入错误");
        }
        /**
         * 实时反馈功能 + new RuntimeException("实时要反馈的异常信息/提示信息/任何想要的返回结构")
         * 上一层 调用 下一层
         * 在上一层 trycatch ,捕获RuntimeException().getMessage() 就能实时捕获运行时异常
         *
         */

    }
}
