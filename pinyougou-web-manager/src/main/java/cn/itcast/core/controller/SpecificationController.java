package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.Specification;

import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import pojogroup.SpecificationVo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/specification")
public class SpecificationController {

//    需要使用Dao层注入:Mapper接口
    @Reference
    private SpecificationService specificationService;

//    Search:首次打开页面,加载数据到页面:初始化页面的部分数据:包含条件查询功能
    /**
     *
     * @param page 需要与前端页面传递数据一样:要点:传递基本数据的要求.传递对象则不用,但需要使用@RequestBody
     * @param rows
     * @param specification @RequestBody(@Request = false )是解决前端没有传参的另一种方法,
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody Specification specification){
//        调用SpecificationService进行查询
        PageResult pageResult = specificationService.search(page, rows, specification);
        return pageResult;
    }

//     add:增加
    /**
     * add:入参:接收前台传递的json对象:entity需要创建entity的Pojo对象,使用@RequestBody接收转换
     * add:返回的是 Result=(ture,'成功/失败')
     */
    @RequestMapping("/add")
    public Result add(@RequestBody SpecificationVo specificationVo){
//      调用serviec层
      Result result = specificationService.add(specificationVo);
      return result;//todo:回头记得返回值
    }

//    update:修改:第一步,根据id查询相应数据,回显:原因:bs架构 实时性不高
    @RequestMapping("/findOne")
    public SpecificationVo update(Long id){
//        1.调用Service层进行查询
        SpecificationVo vo = specificationService.findOne(id);
        return vo;
        /**
         * 此时,返回值中有id,页面中的entity中将有id,页面将会判断出来,后续的保存点击是 修改触发的
         */
    }
//    update:修改:第二步,更新1.表一  2.更新表二
    @RequestMapping("/update")
    public Result update(@RequestBody SpecificationVo specificationVo ){
//        调用service层传递参数
        Result result = specificationService.update(specificationVo);

        return result;
    }

    //    规格管理:关联规格信息到选项框
    @RequestMapping("/selectOptionList")
    public List<Map> selectOptionList(){
//        调用Service层
        List<Map> mapList = specificationService.selectOptionList();
        return mapList;
    }


}
