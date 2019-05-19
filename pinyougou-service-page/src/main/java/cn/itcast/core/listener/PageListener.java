package cn.itcast.core.listener;

import cn.itcast.core.service.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;


//public class PageListener implements MessageListener {
//    @Autowired
//    private StaticPageService staticPageService;
//    @Override
//    public void onMessage(Message message) {
////        1.转换接收到Message类型,转成五大数据类型之一
//        ActiveMQTextMessage addStaticPage = (ActiveMQTextMessage) message;
////        2.取出消息容器中的值
//        try {
//            String id = addStaticPage.getText();
//            System.out.println("静态化项目收到的id->"+id);
////            3.根据消息值id 处理相关业务
////          ---------------------------------//long.parseLong()....
//            staticPageService.index(Long.parseLong(id));
////          ---------------------------------
//        } catch (JMSException e) {
//            e.printStackTrace();
//        }
//
//    }
//}
//jms2consumer.xml中实例化的
public class PageListener implements MessageListener {

    @Autowired
    private StaticPageService staticPageService;//执行业务的代码太长,移到外边了
    @Override
    public void onMessage(Message message) {
        ActiveMQTextMessage atm = (ActiveMQTextMessage) message;
        try {
            String id = atm.getText();
            System.out.println("静态化项目接收到的ID:" + id);
            //3:静态化页面
            staticPageService.index(Long.parseLong(id));

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
