package cn.itcast.core.controller;


import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.service.BrandService;
import com.alibaba.dubbo.config.annotation.Reference;

import entity.PageResult;
import entity.Result;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 */
@RestController
@RequestMapping("/brand")
public class BrandController {

//web 层依赖interface接口 是为了注入 使用这个接口
//service层依赖interface接口 是为了实现这个接口
    @Reference
    private BrandService brandService;
    //获取所有品牌结果集
    @RequestMapping("/findAll")
    public List<Brand> findAll(){
        return brandService.findAll();
    }

//    写分页功能返回 分页对象
//    接收页面传递的入参
    @RequestMapping("/findPage") //     当前页           每页显示条数
    public PageResult findPage(  Integer pageNum, Integer pageSize){
//    到Pojo项目中去把  分页对象 写了
//    到Service层去把业务写掉->然后到ServiceImpl中实现->然后再回到这里调用
        PageResult pageResult = brandService.findPage(pageNum, pageSize);
        return pageResult; //返回数据给页面.success(function(response))中的response了
//    然后回到页面把相应数据遍历出来
    }

//  新增商品
    @RequestMapping("/add")
    public Result add(@RequestBody Brand band){
//        调用serice的查询功能
        Result result = brandService.add(band);
        return result;
//        然后页面就可以接收了
    }

//  修改商品:第一步:根据id查询回显
    @RequestMapping("/findOne")
    public Brand save(long id){
        //使用id:查询
        Brand brand = brandService.findOne(id);
//判断是否真的查询成功了:这里略过
        return brand;
    }

//    修改商品:第二步:根据传参保存数据
    @RequestMapping("/update")
//   不需要返回值
    public Result update(@RequestBody Brand brand){
        try {
            brandService.update(brand);
            return new Result(true,"保存成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"保存失败");
        }
        /**
         * 这里写的超级不严谨,没有真正意义上的判断是否真真的保存成功了,
         * 而是直接人为返回成功与否的操作
         */
    }

    @RequestMapping("/delete")
    public Result delete(long ids){
//        调用Service接口,完善接口
        try {
            brandService.delete(ids);
            return new Result(true,"保存成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"保存失败");
        }
//        不严谨的查询成功,信息返回
//        BUG:删除后,再新增后,id不是连续的,
//            且,删除后的id依然在[]复选框数组当中,
//        但是刷新之后,id在数组中的记录就消失了

    }

//  条件查询:包含查询所有_第一页数据
    @RequestMapping("/search")
    public PageResult search(Integer pageNum,Integer pageSize,@RequestBody Brand brand){
//        实现调用接口
          PageResult pageResult = brandService.search(pageNum,pageSize,brand);
            return pageResult;
    }

    /**
     * 这里是直接返回的List<Map>,是因为前台需要的是不同的遍历对象:自定义的
     * @return
     */
//    模板管理:查询所有品牌分类
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
//        调用service层进行查询
        List<Map> mapList = brandService.selectOptionList();

        return mapList;//todo:返回
    }
}
