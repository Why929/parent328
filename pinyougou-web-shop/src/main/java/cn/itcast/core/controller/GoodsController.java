package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pojogroup.GoodsVo;

@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Reference
    private GoodsService goodsService;
    //添加商品
    @RequestMapping("/add")
    public Result add(@RequestBody GoodsVo vo){
        try {
            //商品ID 主键:这个主键是商家名字,特殊的,不是像其他自增的
//            只能在Controller从SpringSecurity框架中获得
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //完善vo其他信息
            vo.getGoods().setSellerId(name);

            //保存商品对象
            goodsService.add(vo);
            return new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    /**
     *
     * @param page 当前页
     * @param rows 结果集
     * @param goods 前端传递的SearchEntity 对应的就是Goods对象,因为只查goods单表,且条件查询,条件查询,肯定是使用的对象
     * @return
     */
//    web-shop页面商品管理:商品查询:返回PageResult对象:总条数+结果集
    @RequestMapping("/search")
    public PageResult searchM(Integer page,Integer rows,@RequestBody Goods goods){
//分析:后端(平台)接收到页面(商家)的查询请求,这个请求是有权限的,只能查登录用户的,所以,
//     后端要给查询对象配置登录用户id,只有等于该id的信息才能被查到,
//      而SellerId是Seller表的主键:
//        1.获取商家ID 主键:就是Seller表中的主键SellerId,同时也是商户登录Id
//        这里的SpringSecurity中使用的是 todo:回头看闯哥视频
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
//        补全goods对象中的SellerId
        goods.setSellerId(name);
//        2.调用goodsService层进行相关信息查询
        PageResult searchPageResult = goodsService.search(page, rows, goods);
        return searchPageResult;
    }

//    商品修改之 信息回显 根据id查询商品
    @RequestMapping("/findOne")
    public GoodsVo findOne(Long id){
        return goodsService.findOne(id);
    }

//    商品修改按钮触发,更改信息后,单击保存
@RequestMapping("/update")
public Result update(@RequestBody GoodsVo vo){
    try {
        //保存商品对象
        goodsService.update(vo);
        return new Result(true,"成功");
    } catch (Exception e) {
        e.printStackTrace();
        return new Result(false,"失败");
    }
}
}
