package cn.itcast.core.controller;

import cn.itcast.core.pojo.Cart;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
/*
    ListCart<Cart>
                  Cart:商家Id
                       商品结果集 List<OrderItem>
                                                 itemId 库存id
                                                 num    数量
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    CartService cartService;
/*
    @CrossOrigin允许对方浏览器 跨域访问这个服务器
    AbostractList.indexOf() 底层是比较的hashcode,所以重写List存放对象中的对象中某一个属性的hashcold
 */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9003")//,allowCredentials = "true"默认
    public Result findCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){
        try{
//          ----------------cookie中取CartList-------------------------------------------
//            0.声明一个购物车集合:cartList:1.接收从cookie中查询出来的List<Cart> 2.如果没有查询到:则声明一个null
            List<Cart> oldCartList  = null;

            //            B:判断有没有登录:从cookie中取出 购物车那个Cookie Cart : JsonList<Cart>
//            要点:使用xml配置:匿名登录模拟京东
//            1.获取Cookie 数组,方法中声明HttpServlet req/res
            Cookie[] cookies = request.getCookies();
//                1.1 返回一个Cookie数据,找到你存放的CartList 可能不存在
            if (null!=cookies && cookies.length > 0 ){
//            2.从Cookie数组中获取购物车
                for (Cookie cookie : cookies) {
//                        2.1找指定名字的 Cookie
                    if ("CART".equals(cookie.getName())){
                        String cartListJson = cookie.getValue();
//                          2.2转换Json串
                        oldCartList = JSON.parseArray(cartListJson, Cart.class);
//                          2.3如果中途就找到了 那么就提前退出 break
                        break;
                    }
                }
            }
//              要点:到这里:Cookie可能去出来了,也可能没有,CartList可能是null,可能有值
//            3.没有创建购物车:解决从Cookie中没取到CartList的情况,如果为null,则初始化一个CartList
            if (null == oldCartList){
                oldCartList = new ArrayList<>();
            }
//----------------------从Cookie中取出CartList完毕--------------------------------
//              要点:到这里:CartList可能有值:购物车内存在某商家的某商品,
//                   也可能是空的,但不是null,所以判断CartList中有没有一个Cart id 是指定商家id的
//            4.追加当前款: 追加一个Cart ->sellerId
//                                       List<OrderItem> ->itemId
//                                                       ->num
//              --------------新建入参Cart------------------------
//              4.0:首先新建一个Cart,存入 入参商家Id,库存Id,数量
            Cart newCart = new Cart();
//              4.0.1 存商家Id:现在只有 item num,商家id需要到Mysql中取查询,在Buyer中添加相应方法
            Item item = cartService.findItemById(itemId);
            newCart.setSellerId(item.getSellerId());//存入商家Id
//              4.0.2 商品结果集:新建List<OrderItem>
            List<OrderItem> newOrderItemList = new ArrayList<>();
//              4.0.3 商品对象:新建OrderItem:向其中添加ID 数量
            OrderItem newOrderItem = new OrderItem();
//              4.0.4 添加库存id
            newOrderItem.setItemId(itemId);
//              4.0.5 添加数量
            newOrderItem.setNum(num);
//              4.0.6 将newOrderItem添加到List<newOrderItem>新结果集中
            newOrderItemList.add(newOrderItem);
//              4.0.7 将List<newOrderItem>添加newCart中
            newCart.setOrderItemList(newOrderItemList);
//-------------------------入参存入Cart中完毕--------------------------------
//              -----------判断Cookie中是不是有入参Cart-------------
//              4.1判断商家结果集CartList中是不是有 当前款的商家id即入参Cart
//                      4.1.2 查询cartList中有没有指定的 sellerid:参数填的是newCart 而不是商家id
            int newIndex = oldCartList.indexOf(newCart); //i是出现在cartList中的位置
//                    要点:i 如果i 为 -1,则说明没有找到,如果 不为-1,则说明cartList中有相应的 该商家的购物车
//                        判断id,从而判断到底有没有 那个商家的购物车
            //判断:有,取出指定购物车:从商家结果集中找出 跟当前款的商家id是一致的那个购物车
            if (newIndex!= -1){
//              4.2 如果有,则继续判断该商家的cart中是否有当前款商品
                //a.从Cookie中CartList取指定的 Cart
                Cart oldCart = oldCartList.get(newIndex);
                //b.从Cart中拿出List<OrderItem>
                List<OrderItem> oldOrderItemList = oldCart.getOrderItemList();
                //c.从旧List<OrderItem>中取新OrderItem,看能不能取出来:参数比的是对象:newOrderItem
                int newOrderItemIndexOf = oldOrderItemList.indexOf(newOrderItem);
//                      4.2.1 判断该Cart中是不是有指定商品?能取出来就说明newOrderIndex != -1
                if (newOrderItemIndexOf != -1){
//                      4.2.2 如果有则:取出指定的OrderItem追加数量:有可能不是追加一个商品 ,所以是OrderItem.getNum()
                    OrderItem oldOrderItem = oldOrderItemList.get(newOrderItemIndexOf);
//                      4.2.3 从oldOrderItem库存属性对象中取出来  数量,并追加
                    oldOrderItem.setNum(oldOrderItem.getNum()+newOrderItem.getNum());
                }else {//如果从oldOrderItemList取不出来/找不到 入参库存id,那么就说明 没有
//                      4.2.3 如果没有:则添加当前款商品到Cart中的List<OrderItem>商品结果集中
                    oldOrderItemList.add(newOrderItem);
                }
            }else {
//               4.3 如果没有,作为新的商家商品添加到购物车结合中 put in CartList
                oldCartList.add(newCart);
            }
//  ---------------------------判断添加完毕---------------------------------------------------------
/** 未登录
 * 1.从cookie中获取数据 公共
 * 2.获取cookie中的购物车 公共
 * 3.没有 创建购物车
 * 4.追加当前款  有就追加,没有就不追加
 *
 */

/** 已登录
 * 1.从cookie中获取数据 公共
 * 2.获取cookie中的购物车 公共
 * 3.有:将Cookie中的购物车合并到Redis中 清空Cookie
 * 4.从Redis缓存中查出购物车
 * 5.有 将购物车装满 公共
 * 6.回显  公共
 */

//            1.从框架中获取登录名:Security 整合了 CAS1
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
//            2.判断用户是否登录:获取框架中的名字,看 是不是匿名登录 anonymousUser,否则就是登录了
            if (!"anonymousUser".equals(name)){
//              A.登录了,使用安全框架之后,就使用这种方式来判断是不是登录了,判断匿名不匿名,不用判断登录没登录
//               1.将当前购物车合并到Redis中,cookie中的老购物车
                cartService.mearge(oldCartList,name);
//                2.清空Cookie 浏览器的
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);//立即清除
                cookie.setPath("/");//清除路径
                response.addCookie(cookie);//写回浏览器
            }else{
              //B.未登录

// --------------------------首先判断添加到Cookie中,然后
//           5.将当前都无从再添加到Cookie
                Cookie cookie = new Cookie("CART", JSON.toJSONString(oldCartList));
                cookie.setMaxAge(60*60*24);//配置存活时间
//                cookie.setDomain("jd.com")//二级域名下都可以共享该cookie
                cookie.setPath("/");//本域名下本端口下,访问不通的项目路径都可以携带共享该cookie
//            6.回写浏览器
                response.addCookie(cookie);

            }

            return new Result(true,"加入购物车成功");
        }catch (Exception e){
            e.printStackTrace();
        }
        return new Result(false,"加入购物车失败");
    }
//*************************************************************************

    /*
    要点:查询所有:1.cookie中有:则根据cookie中的东西,到数据查询,然后返回
                 2.没有,则不管,因为购物车页面有可能就是空的
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse){
//        声明一个OldCartList
        List<Cart> oldCartList = null;

        //未登录
        //1.获取Cookie数组
        Cookie[] userCookies = httpServletRequest.getCookies();
        if (null!=userCookies && userCookies.length > 0){
            //2.获取Cookie中的购物 车
            for (Cookie cookie : userCookies) {
//                    2.1找到指定的名字
                if ("CART".equals(cookie.getName())){
//                        2.2找到指定名字就 取出对应value
                  //  String oldCartListJson = cookie.getValue();
//                        2.3转换Json串
                    oldCartList = JSON.parseArray(cookie.getValue(), Cart.class);
//                        2.3如中途就找到了,那么就提前退出 break
                    break;
                }
            }
        }



        String name = SecurityContextHolder.getContext().getAuthentication().getName();
//            2.判断用户是否登录:获取框架中的名字,看 是不是匿名登录 anonymousUser,否则就是登录了
        if (!"anonymousUser".equals(name)){
//              登录了,使用安全框架之后,就使用这种方式来判断是不是登录了,判断匿名不匿名,不用判断登录没登录
//       3.将 Cookie中的购物车合并到Redis中
            if (null != oldCartList){
//                合并用户浏览器中的 购物车到redis中,然后清除用户浏览器,然后在conredis中查询出来
                cartService.mearge(oldCartList,name);
//                清空Cookie
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                httpServletResponse.addCookie(cookie);

            }
//            4.从Redis缓存中查出购物车
            oldCartList = cartService.findCartListFromRedis(name);

        }

            //5.有 将购物车装满
            if(null != oldCartList && oldCartList.size() > 0){
                oldCartList = cartService.findCartList(oldCartList);
//                根据购物车中的信息去数据库查询相关信息
            }
            //6.回显


        return oldCartList;
    }
}
