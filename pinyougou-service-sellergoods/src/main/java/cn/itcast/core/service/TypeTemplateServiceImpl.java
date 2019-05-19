package cn.itcast.core.service;

import cn.itcast.core.mapper.specification.SpecificationOptionDao;
import cn.itcast.core.mapper.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Map;

//最最首先要把这个注册到Dubbo当中
@Service
public class TypeTemplateServiceImpl implements TypeTemplateService {
//    注入TypeTemplateDao
    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
//    注入redis
    @Autowired
    private RedisTemplate redisTemplate;

//    search:页面首次打开加载:包含条件查询
    @Override
    public PageResult search(Integer page, Integer rows, TypeTemplate typeTemplate) {
        //页面首次加载的时候,查询所有模板分类数据:放到redis中一份,供搜索页面 回显品牌+规格的时候使用
//        1.查询所有模板分类数据:

        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
//        2.模板信息存入redis中格式brandList 分类Id:品牌name   id就是自己的id即模板的id  分类表中有模板id,所以分类id 对应 模板id
//                               specList   分类Id:规格name   id就是自己的id即模板的id
//        要点:模板中存的是[{},{}] 需要JSON.parse(list,map.class)
        if (null != typeTemplates && typeTemplates.size()>0){


            for (TypeTemplate template : typeTemplates) {
//            1.品牌处理:每个分类下:都有多个品牌
//            [{"id":56,"text":"王总商品二"},{"id":7,"text":"中兴"}]
                String brandIds = template.getBrandIds();
//            返回的是一个List<Map>,之后页面会进行遍历取值,问题:为什么有这么多品牌?因为一个手机分类下 就是有很多品牌
                List<Map> brandMmapList = JSON.parseArray(brandIds, Map.class);
                redisTemplate.boundHashOps("brandList").put(template.getId(),brandMmapList);
                System.out.println(redisTemplate.boundHashOps("brandList").get(template.getId()));
//            2.规格处理:规格是 同规格选项也 一样: 规格+规格选项 之前做过在下面:findBySpecList(Long id),参数是分类id=templateID
                List<Map> spec_SpecOptionList = findBySpecList(template.getId());
                redisTemplate.boundHashOps("specList").put(template.getId(),spec_SpecOptionList);
                System.out.println(redisTemplate.boundHashOps("specList").get(template.getId()));
            }
//        到此 分类表+品牌表+规格表 都存入到rendis中了,redis到此还有一个 轮播图表
//        接下来去写 service-search 中写,搜索关键字后,分类栏中 第一个分类的 详细品牌 规格信息 到页面结果信息头方法,所有关键字后,商品信息回显方法已经写反
        }

//        ----------------------------------------------------------------------------------------------------
//        首先把分页信息配置到PageHelper中
        PageHelper.startPage(page,rows);

//        查询所有:
//        1.创建查询对象:todo:先查询所有null
        Page<TypeTemplate> t = (Page<TypeTemplate>) typeTemplateDao.selectByExample(null);
//        2.最终要返回PageResult对象:
        PageResult pageResult = new PageResult(t.getTotal(), t.getResult());
        return pageResult;
    }

//  add 新增一条TypeTemplate表中的一条记录
    @Override
    public void add(TypeTemplate typeTemplate) {
//        调用Dao层执行insert操作
        typeTemplateDao.insertSelective(typeTemplate);
    }

//  回显:修改按钮触发的操作
    @Override
    public TypeTemplate findOne(Long id) {
//        根据主键id 查询单条对象
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
        return typeTemplate;
    }

//    更新:update.updateByPrmaryKeySelective(TypeTemplate typeTempate)
    @Override
    public void update(TypeTemplate typeTemplate) {
//        更新:根据主键 选择更新对象
      typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);//返回影响行数

    }

//查询规格表页面的信息:表A + 表B
//表A:specification   表B:specification_option
    @Override
    public List<Map> findBySpecList(Long id) {
//    1.通过ItemCat表中的模板Id查询 模板对象:BrandIds SpecIds Customer
        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);
//        2.取出typeTemplate模板中的specification规格简化对象
        String specIds = typeTemplate.getSpecIds();
//        3.将Json字符串specId 解析成对象[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}],解析出来是List<map>
        List<Map> mapList = JSON.parseArray(specIds, Map.class);
        if(null != mapList && mapList.size() >0){

//        4.将specification_option表中的对象 加入到Map中,使每个Map中有3个属性{"B":xx,"C":yy,"D":zz}  "D":zz 就是option options:List<options>
//          每一个specId对象对应 多个 规格属性,因此,Map中第三个属性是 specification_option表中的一些对象->所以是一个List=>List<option>
            for (Map map : mapList) {
//            5.根据specId查询其对应的Option对象集合:
//                      [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}],解析出来是List<map>
//                      specification_option id 存在 map中,key="id":value=27
//            所以,用"id"取出来每次遍历的值,另外,map.get("id")获得到的是Object对象
//                6.创建条件查询对象

                SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
//            7.完善条件对象
                specificationOptionQuery.createCriteria().andSpecIdEqualTo(new Long((Integer)(map.get("id"))));
//                8.执行查询语句.
                List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
//            9.将查询出来的结果放入到Map中
                map.put("options",specificationOptionList);
            }
        }
        return mapList;
       // typeTemplateDao.insertSelective()
        //
    }
}
