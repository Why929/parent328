package cn.itcast.core.controller;

import cn.itcast.core.service.PayService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    PayService payService;

    @RequestMapping("/createNative")
    public Map<String,String> createNative(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> map = payService.createNative(name);
        return map;
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){//与页面传参一样
        try {
            int x = 0;
            while (true){
                Map<String,String> map = payService.queryPayStatus(out_trade_no);
                System.out.println("支付状态:"+map.get("trade_state"));
                if ("SUCCESS".equals(map.get("trade_state"))){
//                    修改支付日志为 已支付,修改订单已支付,redis中的待支付日志对象需要删除
//                    todo:orderService.updatePayLogAndOrderStatus(out_trade_no)
//                    todo:
                    return new Result(true,"支付成功");
                }else {
                    Thread.sleep(3000);
                    x++;
                    if (x>100){
//                        调用微信服务器端:失败 todo:
//                        payService.closePay(out_trade_no):项目衔接
                        return new Result(false,"二维码超时");

                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"查询失败");
        }
    }

}
