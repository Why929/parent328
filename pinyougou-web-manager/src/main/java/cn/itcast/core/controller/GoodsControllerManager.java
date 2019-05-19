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

import java.io.Reader;

@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class GoodsControllerManager {
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
     * @param page 当前页 首次加载的时候,初始值 是由页面给的,之后是点击页码传递的
     * @param rows 结果集 首次加载的时候, 初始值是由页面给的,
     * @param goods 前端传递的SearchEntity 对应的就是Goods对象,因为只查goods单表,且条件查询,条件查询,肯定是使用的对象
     * @return
     */
//    web-Manager页面商品管理:商品查询:返回PageResult对象:总条数+结果集
    //商品审核:回显的是 goods表的信息
    @RequestMapping("/search")             // 接收查询条件:{auditStatus:'0'}封装到Goods对象中
    public PageResult searchM(Integer page,Integer rows,@RequestBody Goods goods){
//      1.运营商平台:查询所有,区别于同时访问goodService层的商家后台,不需要 传递当前登录用户的 用户名
//      2.调用goodsService层进行相关信息查询
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

    /**
     *
     * @param ids:是Goods表的主键
     * @return
     */
//    商品审核:删除
    @RequestMapping("delete")
    public Result delete(Long[] ids){
        try {
            goodsService.delete(ids);
            return   new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return   new Result(false,"失败");
        }
    }

    /**
     *
     * @param ids :是Goods表的主键
     * @param status
     * @return
     */
//    status :到底是什么类型:需要到数据库去看
//    商品审核:驳回  通过 都使用一个controller
    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids,String status){
        try {
            goodsService.updateStatus(ids,status);
            return   new Result(true,"成功");
        } catch (Exception e) {
            e.printStackTrace();
            return   new Result(false,"失败");
        }
    }
}
