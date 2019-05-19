package cn.itcast.core.service;

import cn.itcast.core.mapper.good.BrandDao;
import cn.itcast.core.pojo.good.Brand;
import cn.itcast.core.pojo.good.BrandQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 品牌管理
 */
@Service
@Transactional
public class BrandServiceImpl implements  BrandService {

    @Autowired
    private BrandDao brandDao;
    //查询
    public List<Brand> findAll(){
        return brandDao.selectByExample(null);
    }
    /*
    当前页+每页显示条数 是到数据库查询需要使用的
    结果集+总条数      是查询计算出来返回给前台的
     */

    @Override //                        当前页       每页显示条数
    public PageResult findPage(Integer pageNum, Integer pageSize) {
        //2.分页插件:效果:该语句后面的一个查询结果将会被分页插件自动做分页处理.
        PageHelper.startPage(pageNum, pageSize);
        //3.数据查询:使用brandDao.SelectByExample(入参:都是什么?),
        /**
         * Page extends ArrayList,进行了更加详细的封装
         */
        Page<Brand> p = (Page<Brand>) brandDao.selectByExample(null);
        //4.然后再从Page<Brand>.结果集/总条数 赋值到PageResult
        PageResult pageResult = new PageResult(p.getTotal(), p.getResult());
        return pageResult;
    }

    @Override
    public Result add(Brand band) {
        /**
         * Resule中有两个属性:Boolean flag
         *                   String  message
         *构造函数的效果:new Result(属性一,属性二)
         * 解释:不用分步的赋值:简化代码
         * 保存失败时:同时赋值flag message 属性
         */
        try {
            int i = brandDao.insertSelective(band);
            //其实应该判断一下返回值时是否大于0,然后知道是否保存成功
            return new Result(true,"保存成功");
        } catch (Exception e) {
            return new Result(false,"保存失败");
//            e.printStackTrace();
//            然后前台就可以接收了
        }

    }
//修改商品:第一步:回显要修改的商品
    @Override
    public Brand findOne(Long id) {
//        根据主键id查询结果
        Brand brand = brandDao.selectByPrimaryKey(id);
        return brand;
    }
//修改商品:第二步:执行修改操作->保存数据
    @Override
    public void update(Brand brand) {
//        使用根据主键修改:updateByprimaryKeySelectice(brand)
        brandDao.updateByPrimaryKeySelective(brand);
//        判断是否保存成功,这里省略
    }

//    删除商品
    @Override
    public void delete(long ids) {
//        调用dao层,执行删除
        /**
         * 删除使用的是deleteByExample(查询对象)进行删除的.
         * 查询对象:可以执行诸如: select * from table where id in(1,2,3) ..
         * 而查询对象就是从 where 之后的东西 where id in (1,1,2)
         * 优势:可以一次性操作一堆
         */
//        创建条件对象,
//        使用条件对象中的createCriteria:where对象
//        使用where对象中的:and id in 方法)(入参集合):
//        数组转集合方法:Arrays.asList()
        BrandQuery brandQuery = new BrandQuery();
        /**
         * 注意不能:不能返回一个criteria对象,
         * 因为brandQuery.createCriteria()相当于 student.set(),最后使用的是 student,这里也是,最后使用的是BrandQuery对象
         * 却别:createCriteria()之内,还有一个set(入参)方法,
         * 所以若是分开写,最后传入的是BrandQuery对象
         * 或者链式编程方式写:
         * brandQuery.criteria().andIdIn(Array arr) ,最后还是传入brandQuery对象
         */
        brandQuery.createCriteria().andIdIn(Arrays.asList(ids));
//        控制台输出打印变量
        System.out.println(brandQuery);
        brandDao.deleteByExample(brandQuery);
    }

    @Override
    public PageResult search(Integer pageNum, Integer pageSize, Brand brand) {
        //        在查询语句上面,执行分页助手的pageHelper,自动将身后第一条语句进行分页操作
        PageHelper.startPage(pageNum,pageSize);
//        先判断 查询对象是不是为空:
//        不完整要点_带Bug:如果brand为空:则为查询所有_首页
//        不为空,则创建条件查询对象,从Brand中取出查询条件,赋值给查询对象
//        要点:是给的Criteria()的,因为Criteria()是属于BrandQuery的,它只能有一个Criteria()
//        而Criteria()则能进一步接收 更为具体的条件,且能追加添加条件,可以链式编程
//        问题一:是不是需要两个Service方法?
//        答:不知道啊
//        首先声明一个 条件对象,然后把createCriteria()单独声明出来
        BrandQuery brandQuery = new BrandQuery();
//      测试:如果条件都为空,那么 selectByExample(传入一个空的值,会怎么样?)
        System.out.println("如果传入的brandQuery条件对象是空的,那么输出结果会怎么样?==>"+brandQuery);
        BrandQuery.Criteria criteria = brandQuery.createCriteria();
//      模糊查询:这里选择了忽略大小写:不知道对中文是不是生效
        if (null!=brand.getName()&&!"".equalsIgnoreCase(brand.getName().trim())){
            //如果不为空,将这个条件 设置到Criteria()中,事实上是追加感觉是
//            模糊查询的.andXxxLike("%" + condition +"%"):
//            要点:'%' 是需要手动加上的
            criteria.andNameLike("%"+brand.getName()+"%");
        }
        if (null!=brand.getFirstChar()&&!"".equalsIgnoreCase(brand.getFirstChar().trim())){
//            如果不为空,将这个条件 设置到Criteria()中,
//            要点:注意:条件有firstChar不等于 notEqualTo 这个该死的讨价查询
            criteria.andFirstCharEqualTo(brand.getFirstChar());
        }

//        将返回的List<brand> 使用自己封装的Page<Brand>进行接收:内部进行了更加详细的 值的划分
        Page<Brand> brands = (Page<Brand>) brandDao.selectByExample(brandQuery);
//        将Page<Brand>中的结果集,总条数取出来,赋值给PageResult对象中的结果集/总条数
        PageResult pageResult = new PageResult(brands.getTotal(), brands.getResult());
        return pageResult;
    }

    @Override
    public List<Map> selectOptionList() {
        /**
         * 原先查询所有,返回List<brand>集合,但是现在需要放到List<Map>中,就需要把List<brand>中的key/value拿出来,
         * 在put到map中,然后再将map add到List<Map>中,当是现在可以手写sql,直接返回List<Map>
         */
//        调用Dao层进行查询
        List<Map> mapList = brandDao.selectOptionList();
        return mapList;
    }
}

