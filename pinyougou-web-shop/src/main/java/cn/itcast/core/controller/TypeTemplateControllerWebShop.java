package cn.itcast.core.controller;

import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 这个@RequestMapping 同Web-Manager中的@RequestMapping是一样的,
 * 没有影响,因为这个是加载到了不同的
 */
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateControllerWebShop {
//    注入dubbo service层
    @Reference
    private TypeTemplateService typeTemplateService;

    /**
     * @param page 当前页
     * @param rows 每页显示条数
     * @param typeTemplate 条件查询入参
     * @return PageResult :总条数  结果集
     */
//    首页回显:应该包含条件查询//todo:条件查询
    @RequestMapping("/search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate ){
//        要点:返回的是 PageResult 最终
//        调用Service层进行查询:上面需要注入service层
        PageResult pageResult = typeTemplateService.search(page, rows, typeTemplate);

        return pageResult;
    }

//    add:添加新的规格管理:
    /**
     * 要点:这是一个单张表的操作:
     *      虽然使用到了其他两张表的数据,但是,那两张表的数据以另外一种形式进行的查询,
     *      并且,回显到了 页面$scope域中:使得选中哪个品牌分类/选择哪个规格分类 选项后,
     *      entity.brandIds/entity.specIds 就被赋值了 对应的json串
     *      最后提交的参数是 建好名+选好品偶分类+选好规格分类的 entity
     */
    @RequestMapping("/add")
    public Result add(@RequestBody TypeTemplate typeTemplate){//是由前端页面 选择好,然后传过来固定的Json串的
//                                                               然后 这边接收过来 直接转成对象的
//                                                              而该对象,又可以传回页面,遍历成选项标签 中的每一项
//        调用service层
        try {
            typeTemplateService.add(typeTemplate);
            return new Result(true,"成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(true,"失败");
        }
    }

//    findOne:修改按钮的回显功能:实质:根据id查询typeTemplate表中的一条数据:
//            返回的是TypeTemplate对象 而不是PageResult对象?因为页面中封装的对象 不一样
    @RequestMapping("/findOne")
    public TypeTemplate findOne(Long id){//要点:接收基本类型的时候:不需要加@RequestBody
        TypeTemplate typeTemplate = typeTemplateService.findOne(id);
        return typeTemplate;
        /**
         * 在Web-shop项目中,同样是调用Interface项目中中TypeTemplateService接口实现了
         */
    }
//    修改:update 更新操作
    @RequestMapping("/update")
    public Result update(@RequestBody TypeTemplate typeTemplate){//要点:接收基本类型的时候,必须增加@ResquestBody
        try {
            typeTemplateService.update(typeTemplate);
            return new Result(true,"成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

//   规格页面: 规格选项表:typeTemplate_option选项 对象查询
    /**
     *规格页面:是两张表的结果,每一行是:表A对象+表B对象 -->Map  表A_:_  ,  表B_:_
     *                      有很多行:所以返回的是   List<Map>
     *  表A是specification  表B是Specification_Option
     */
    @RequestMapping("/findBySpecList")
    public List<Map>  findBySpecList(Long id){
        List<Map> listMap = typeTemplateService.findBySpecList(id);

        return listMap;
    }
}
