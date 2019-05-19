package cn.itcast.core.service;

import cn.itcast.common.utils.HttpClient;
import cn.itcast.core.pojo.log.PayLog;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class PayServiceImpl implements PayService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;

//    远程调用腾讯那边服务器
//    查询微信官方Api文档
    @Override
    public Map<String, String> createNative(String name) {

//        1.直接获取支付日志的Id
        PayLog payLog = (PayLog) redisTemplate.boundHashOps("payLog").get(name);
//        .支付Id 合并后
//        .金额
//        1.调用同一下单Api
//          发送Https请求? Https Apache HttpClient
         Map<String,String> param = new HashMap<>();
//        2.公众号id
        param.put("appid",appid);
//        3.商户号
        param.put("mch_id",partner);
//        4.随机字符串 nonce_str
        param.put("nonce_str", WXPayUtil.generateNonceStr());
//        5.商品描述
        param.put("body","外闹特");
//        6.商户订单号 out_trade_no
        param.put("out_trade_no",payLog.getOutTradeNo());
//        7.标价金额:分total_fee
        param.put("total_fee","1");
//  ```   8.终端Ip    spbill_create_ip
        param.put("spbill_create_ip","127.0.0.1");
//        9.通知地址   notify_url
        param.put("notify_url","www.wwww.cn");
//        10.交易类型  trade_type
        param.put("trade_type","NATIVE");
        try {
//        11.签名:加密得到xml
            String xml = WXPayUtil.generateSignedXml(param, partnerkey);
//        12.跳转地址:支付地址
            String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";
//        13.使用HttpClient 工具类发送
            HttpClient httpClient = new HttpClient(url);
//        14.配置Https 安全传输是否开启
            httpClient.setHttps(true);
//        15.入参 Map实际字符串
            httpClient.setXmlParam(xml);
//        16.配置提交参数
            httpClient.post();
//        17.获取响应信息
            String result = httpClient.getContent();
            System.out.println(result);
//        18.将响应信息 转成Map
            Map<String, String> map = WXPayUtil.xmlToMap(result);
//        19.响应 支付ID
            map.put("out_trade_no",payLog.getOutTradeNo());
            map.put("total_fee",String.valueOf(payLog.getTotalFee()));
            return map;


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Map<String, String> queryPayStatus(String out_trade_no) {
        //发出Https请求? Https Apache HttpClient
        Map<String, String> param = new HashMap<>();
//        1.公众号:
        param.put("appid", appid);
//        2.商户号:
        param.put("mch_id", partner);
//        3.随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
//        4.支付订单号:
        param.put("out_trade_no", out_trade_no);
//
        try {
//        5.签名类型
            String xml = WXPayUtil.generateSignedXml(param,partnerkey);
//        6.调用微信查询 接口:一个网址    :使用HttpClient访问,需要传参:在下面继续完善参数
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";
            HttpClient httpClient = new HttpClient(url);
//        7.配置访问方式   :是否为加密类型
            httpClient.setHttps(true);
 //       8. 入参 :访问参数 加密后 ->xml 格式参数
            httpClient.setXmlParam(xml);
    //    9.请求方式post
            httpClient.post();
    //    10.回调参数 String xml格式
            String result = httpClient.getContent();
//        11.解析为Map格式
            Map<String, String> map = WXPayUtil.xmlToMap(result);
//            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
