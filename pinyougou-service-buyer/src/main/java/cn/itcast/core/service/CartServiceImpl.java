package cn.itcast.core.service;

import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    ItemDao itemDao;

//    1.通过Id查询Item对象
    public Item findItemById(Long id){
        return itemDao.selectByPrimaryKey(id);
    }

    public Item selectByPrimaryKey(Long itemId){
        Item item = itemDao.selectByPrimaryKey(itemId);

        return item;
    }

    @Override
    public List<Cart> findCartList(List<Cart> oldCartList) {
        //商家Id 有了
        //商家名称 没有
        for (Cart cart : oldCartList) {
//            1.取出每个购物车中的OrderItem
            List<OrderItem> orderItemList = cart.getOrderItemList();
//            2.填充每个购物车中List<OrderItem> 每个OrderItem信息
            for (OrderItem orderItem : orderItemList) {
//            1.根据库存Id查询所有Cart中的相关信息:都在Item对象中
               Item item = findItemById(orderItem.getItemId());//该方法向上提,在最上面
//            2.向orderItem中 补全各种信息
                //数量:有了
                //图片:
                orderItem.setPicPath(item.getImage());
                //标题:
                orderItem.setTitle(item.getTitle());
                //单价:
                orderItem.setPrice(item.getPrice());
                //小计: .doubleValue() 解决小数点  *orderItem.getNum() 得到总价  new BigDecimal(接各种参数)
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                //商家名称:给该for循环外的 变量赋值,
                cart.setSellerName(item.getSeller());


            }

        }
        return oldCartList;//返回结果集
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void mearge(List<Cart> newCartList, String name) {
//    1.获取原来的购物车结果集
        List<Cart> oldCartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(name);
//    2.新老车大合并
       oldCartList = mearge1(newCartList,oldCartList);
//    3.合并后的老车结果集保存进缓存
redisTemplate.boundHashOps("cartList").put(name,oldCartList);
    }

    @Override
    public List<Cart> findCartListFromRedis(String name) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(name);
        return cartList;
    }

    //新车老车合并
    private List<Cart> mearge1(List<Cart> newCartList, List<Cart> oldCartList) {
//        1.如果老车不等于空
        if (null != oldCartList){
            if (null!= newCartList){
                for (Cart newCart : newCartList) {
//                    1.判断当前款的商家Id 商家结果集中是否有 新添加的
                    int newIndex = oldCartList.indexOf(newCart);
                    if (newIndex != -1){
                        //有:从商家结果集中找出跟当前款的商家id一致的那个购物车对象
                        Cart oldCart = oldCartList.get(newIndex);
                        //此购物车对象中商品结果集中是否包含当前商品
                        List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();

                        List<OrderItem> newOrderItemList = newCart.getOrderItemList();

                        for (OrderItem newOrderItem : newOrderItemList) {
                            int newOrderItemIndexOf = oldOrderItemList.indexOf(newOrderItem);
                            if (newOrderItemIndexOf != -1){
//                                有 追加数量
                                OrderItem oldOrderItem = oldOrderItemList.get(newOrderItemIndexOf);
                                oldOrderItem.setNum(newOrderItem.getNum()+oldOrderItem.getNum());
                            }else {
//                                没有 当新款添加一个
                                oldOrderItemList.add(newOrderItem);
                            }
                        }
                        
                    }else {
                        //没有 作为新的商家的商品添加到购物车集合中
                        oldCartList.add(newCart);
                    }
                }
            }
        }else {
            //返回新车
            return  newCartList;
        }
        //返回老车
        return oldCartList;
    }

}
