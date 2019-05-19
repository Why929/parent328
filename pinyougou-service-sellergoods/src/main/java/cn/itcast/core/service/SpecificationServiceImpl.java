package cn.itcast.core.service;

import cn.itcast.core.mapper.specification.SpecificationDao;
import cn.itcast.core.mapper.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import entity.Result;
import org.springframework.data.redis.core.RedisTemplate;
import pojogroup.SpecificationVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 问题:search,是不是只查询回显的规格表,而没有查询规格选项表?
 */
@Service //注册到dubbo
@Transactional //加事务
public class SpecificationServiceImpl implements SpecificationService {
//   Search:specificationDao 依赖注入规格表Dao:因为查询的是规格表对象:注入的是Mapper接口文件,这里稍有遗忘:要点:使用的是@Autowired:注入的是bean
    @Autowired
    private SpecificationDao specificationDao;

//    add:specificationOptionDao操作specificationOption对象
    @Autowired
    private SpecificationOptionDao specificationOptionDao;


//  Search:首次打开页面,加载数据到页面:初始化页面的部分数据:包含条件查询功能
//  Search:条件查询:包含没有条件的时候
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {

//首先把 分页数据设置了
        PageHelper.startPage(page,rows);
        //判断是不是条件查询,对条件对象进行查询
//        1.首先创建一个条件对象
        SpecificationQuery specificationQuery = new SpecificationQuery();
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();
//        2.进行入参判断:
        if (null!=specification.getSpecName()&&!"".equals(specification.getSpecName().trim())){
            criteria = specificationQuery.createCriteria().andSpecNameLike("%"+specification.getSpecName()+"%");
        }
//        3.进行查询:使用Page<Specification> 进行接收
        Page<Specification> p = (Page<Specification>) specificationDao.selectByExample(specificationQuery);
//        4.将查询到的结果集/总条数 设置到PageResult中,返回PageResult
        PageResult pageResult = new PageResult(p.getTotal(), p.getResult());
        return pageResult;
    }

//    add:增加:操作两张表:1.规格表 2.规格选项表
//    要点:表1 与 表2 的主外键映射关系,是手动完成的映射的,因此需要返回规格表的主键id,
//         在specificationDaoMapper中insert标签中开启:返回主键id:到查询对象vo.speicification.id中
//    上述回显id怎么实现的?不知道//todo:是不是能有此一问?id是被映射回去的
    @Override
    public Result add(SpecificationVo specificationVo) {


        try {
            //   1.操作规格表:调用规格表的Specification的dao.jar mapper接口bean,在dao项目中:插入的是规格表对象
//        1.1到specificationDaoMapper中开启insert返回主键id;
            int i = specificationDao.insertSelective(specificationVo.getSpecification());//返回的是i  是影响行数?还是id?因为是int所以是影响行数 可以用它判断是否插入成功
//   2.操作规格选项表:要点:插入specificationOption对象,所以需要注入/引用specificationOptionDao Mapper接口bean
//        2.1 此时specificationOptionList集合中specification对象不是完整的,因为:规格选项中的外键spec_id还没添加 规格分类id主键
//        解释:操作来源:新建规格:1.规格分类名称 i3处理器 id 2.规格选项集合: [{五代 5100},{七代 7100},{九代 9100}]  spec_id:存放id 规格分类的名称i3处理器
//        结果:i3处理器规格分类下,有三代产品:五代一个型号,七代一个型号,九代一个型号
//          表1:id      key:value
//          表2:表1_id  key1:value
//              表1_id  key2:value
//              表1_id  key3:value
//        所以:需要先将表2集合中的表1_id给赋上值,而这个值,只有执行完 规格分类表insert操作之后,才能被返回->底层映射到Vo.specification.id中
//        2.1将规格分类id 关联到 规格分类选项集合中每一个规格选项中的:外键:spec_id:规格分类id上,因为是集合,所以这是一个循环操作
//        2.1获取返回主键id
            Long id = specificationVo.getSpecification().getId();
/**
             * //        2.2循环将主键id添加到每一个specificationOption对象的外键上:spec_id
             for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
             specificationOption.setSpecId(id);
             }
             //        2.3执行规格分类表的插入操作:插入操作也是一个循环插入:因为每次只能插入一个
             for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
             //            2.4循环插入:每循环出来一个,进行一次插入操作
             specificationOptionDao.insertSelective(specificationOption);
                }
 */
//        优化可以将上述连个操作合并成同一个操作:
            for (SpecificationOption specificationOption : specificationVo.getSpecificationOptionList()) {
//        2.2循环将主键id添加到每一个specificationOption对象的外键上:spec_id
                specificationOption.setSpecId(id);
//        2.4循环插入:每循环出来一个,进行一次插入操作
                specificationOptionDao.insertSelective(specificationOption);
            }
//            //todo:简单的插入成功与否的 判断返回操作
            return new Result(true,"成功");
        } catch (Exception e) {
//            e.printStackTrace();
            return new Result(false,"失败");
        }
    }

    @Override
    public SpecificationVo findOne(Long id) {
        SpecificationVo specificationVo = new SpecificationVo();

//        1.规格分类表信息查询:分类表主键
        Specification specification = specificationDao.selectByPrimaryKey(id);
//        2.赋值到SepcificationVo对象中
        specificationVo.setSpecification(specification);
//        3.规格分类详情表查询:使用条件查询对象xxxQuery.creatCerteria().and查询条件属性EqualTo(condition)
        SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
//        拼接sql:实质:进一步完善条件查询对象的信息
        specificationOptionQuery.createCriteria().andSpecIdEqualTo(id);
//        4.规格选项表查询:使用条件查询对象进行查询
        List<SpecificationOption> specificationOptionList = specificationOptionDao.selectByExample(specificationOptionQuery);
//        2.将specificationOptionList结果集 赋值到speicitionVo中
        specificationVo.setSpecificationOptionList(specificationOptionList);
//        5.将vo返回
        return specificationVo;
    }

//    修改update:
    @Override
    public Result update(SpecificationVo specificationVo) {
        try{
//        1.修改表1
            specificationDao.updateByPrimaryKeySelective(specificationVo.getSpecification());
//        2.修改表2
//        2.1先根据规格分类id,删除表2中所有外键=id的分类选项,然后选重新存入最新的
//        要点:删除:使用的是条件对象进行删除:因为是删除多条
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();
//        完善sql where 后半部分条件:id在vo中
        /**
         * 一开始没有删成功,是因为分类选项表中的外键名是spec_id 而不是id,正确为:表2.andSpec_idEqualTo(表1.id)
         */
            specificationOptionQuery.createCriteria().andSpecIdEqualTo(specificationVo.getSpecification().getId());
//        执行删除
        int i = specificationOptionDao.deleteByExample(specificationOptionQuery);
        System.out.println("吗的删除成功了么?==>"+i);
//        2.2循环遍历存入最新的specificationOptionList
//            首先获取出来specificationOpetion对象:
            List<SpecificationOption> specificationOptionList = specificationVo.getSpecificationOptionList();
//           2.2.1存之前首先 将specificationOption中的spec_id补全:重新给他分配 类别
//           2.2.2然后在遍历存,因为id需要给每一个specificationOption赋值,将2.2.1同2.2.2一起放在循环里面
            for (SpecificationOption specificationOption : specificationOptionList) {
//            2.2.1
                specificationOption.setSpecId(specificationVo.getSpecification().getId());
//            2.2.2
                specificationOptionDao.insertSelective(specificationOption);
            }
            /**
             * 上述操作应该放到try cathch中进行执行
             */
            return new Result(true,"成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,"失败");
        }

    }

    @Override
    public List<Map> selectOptionList() {
//        调用Dao,使用手动映射Sql ,需要去对应的接口 及 Mapper文件中进行相应的添加,并且手写sql动态标签
        List<Map> mapList = specificationDao.selectOptionList();
        return mapList;
    }
}
