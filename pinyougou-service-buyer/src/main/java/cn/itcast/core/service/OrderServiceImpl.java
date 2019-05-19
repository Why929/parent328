package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.mapper.log.PayLogDao;
import cn.itcast.core.mapper.order.OrderDao;
import cn.itcast.core.mapper.order.OrderItemDao;
import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class OrderServiceImpl implements OrderService {
   @Autowired
   private OrderDao orderDao;
   @Autowired
   private OrderItemDao orderItemDao;
   @Autowired
   private IdWorker idWorker;
   @Autowired
   private RedisTemplate redisTemplate;
   @Autowired
   private ItemDao itemDao;
   @Autowired
   private PayLogDao payLogDao;


    @Override
    public void add(Order order) {
//        打印一下这个Order都是什么:是不是只有登录用户名
        System.out.println(order);
        //保存 订单表 订单详情表

        //1.购物车结果集 从reids中根据 订单UserId查询出来
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
//      总金额初始化
        double tp = 0;
//        2.订单Id集合:支付订单paylog需要 整个订单的 小订单号集合Order List<Order>
        List<String> ids = new ArrayList<>();

//        遍历redis取出来的CartLIst集合:每个Cart中都需要完善信息
        for (Cart cart : cartList) {
            //1.保存订单表 到mysql
//            2.订单Id分布式Id生成器
            long id = idWorker.nextId();
//            转换后放到订单Id集合中
            ids.add(String.valueOf(id));
//            3.完善传递过来的参数:
            order.setOrderId(id);
//            3.实付金额 初始化声明一个
            double totalPrice = 0;
//            4.支付类型
            order.setPaymentType("1");
//            5.支付状态
            order.setStatus("1");
//            6.创建时间
            order.setCreateTime(new Date());
//            7.更新时间
            order.setUpdateTime(new Date());
//            8.订单来源
            order.setSourceType("2");
//            9.商家Id
            order.setSellerId(cart.getSellerId());
//            10.完善订单详情表信息: 因为保存第二张表的 必须到Order内部来
            List<OrderItem> orderItemList = cart.getOrderItemList();
            for (OrderItem orderItem : orderItemList) {
//                1.完善每一个购物车中的每一个 商品结果集中每一个商品信息
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //2.配置OrderItem表自己的id
                long oid = idWorker.nextId();
                orderItem.setId(oid);//OrderItem表自己的id
//                3.外键订单主表的Id:关联Order表的Id,在上面 这里建立两张表的Id
                orderItem.setOrderId(id);
//                4.商品Id
                orderItem.setGoodsId(item.getGoodsId());
//                5.标题
                orderItem.setTitle(item.getTitle());
//                6.单价
                orderItem.setPrice(item.getPrice());
//                7.数量已经有了
//                8.小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
//                9.上面订单表总金额的计算:上面进行了初始化
                totalPrice += orderItem.getTotalFee().doubleValue();
//                10.
                tp += totalPrice;
//                11.商家Id
                orderItem.setPicPath(item.getImage());
//                12.保存订单详情表 到Mysql
                orderItemDao.insertSelective(orderItem);
            }
//            13.给Order订单表配置一下总金额
            order.setPayment(new BigDecimal(totalPrice));
//            14.保存订单表信息 到Mysql
            orderDao.insertSelective(order);
        }
//        15.清空购物车
        redisTemplate.boundHashOps("cartList").delete(order.getUserId());

//        16.支付日志表 保存完毕后,将Order 集合信息 添加到支付表中一下
        PayLog payLog = new PayLog();
//        17.id
        long payLogId = idWorker.nextId();
        payLog.setOutTradeNo(String.valueOf(payLogId));
//        18.创建时间
        payLog.setCreateTime(new Date());
//        19.总金额
        payLog.setTotalFee((long)(tp*100));//单位分
//        20.买东西的用户
        payLog.setUserId(order.getUserId());
//        21.支付状态
        payLog.setTradeState("0");
//        22.订单结果集
        payLog.setOrderList(ids.toString().replace("[","").replace("]",""));
//        23.支付类型
        payLog.setPayType("1");
//        24.保存Mysql数据一份
        payLogDao.insertSelective(payLog);
//        25.保存到缓存中一份          这个key         这个子key           这个value
        redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);

    }
}
