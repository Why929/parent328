package cn.itcast.core.service;

import cn.itcast.core.mapper.good.GoodsDao;
import cn.itcast.core.mapper.good.GoodsDescDao;
import cn.itcast.core.mapper.item.ItemCatDao;
import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实现ServletContextAware接口 注入ServletContext变量
 */
@Service//service.xml中实例这个的
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware {
//    1.使用springmvc中的view 中的freemaker
//    1.但是这个需要被实例化首先,所以到本项目中的xml中取实例化 FreemarkerConfigure
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Override
    public void index(Long id){
//        1.获得一个freeMarker对象//io 取:并且需要到xml中配置 默认编码集
        Configuration configuration = freeMarkerConfigurer.getConfiguration();

//        4.声明一个输出路径,放在webapp先能被用户访问到
        String path = "/" + id + ".html";
        String realPath = getRealPath(path);
//        -----------------数据导入------------------
//        5.声明一个数据输入
        Map<String,Object> map = new HashMap<>();
//                                                                             ┌ 相同id  ┐         有goods_id
//        7.存什么看 模板对象中 需要什么,三张基础表 goods goodsDesc  item  商品表小字段 商品详情表大字段 库存条,观察本次使用到的模板文件中对应位置使用的就是这个张表中的数据
//          审核商品页面:回显的是Goods表 之后要生成这张表的静态化页面
//          静态化页面:Goods GoodsDesc item 三张表的信息,
//          而目前只有 Goods表中的信息可以用
//        又有关系:Goods GoodsDesc 两张表的id相同 item表中有Goods_Id
//        至此.三张表的数据都可以被查询出来
//        7.1 根据Goods表id查询Goods表信息,传递给Freemaker进行生成,Goods表id是由回显商品审核页面,点击审核时,传递的一个id
        Goods goods = goodsDao.selectByPrimaryKey(id);
        map.put("goods",goods);
//        7.1 根据Goods表id查询GoodsDesc表信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        map.put("goodsDesc",goodsDesc);
//        7.1 根据Goods表id查询item表信息,因为goodsId不是item表的主键,所以,需要使用itemQuery对象承载查询条件
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id).andStatusEqualTo("1");//web-manager页面可以能缺少一个更改状态的上下架功能
        List<Item> items = itemDao.selectByExample(itemQuery);
        map.put("itemList",items);
//        7.1 根据Goods表id查询item表中item_cat id,使用item_cat id查询 item_cat表
        Long category3Id = goods.getCategory3Id();//上面刚刚查过这里直接用
        Long category2Id = goods.getCategory2Id();//上面刚刚查过这里直接用
        Long category1Id = goods.getCategory1Id();//上面刚刚查过这里直接用
        ItemCat itemCat3 = itemCatDao.selectByPrimaryKey(category3Id);//获得分类对象
        String itemCat3Name = itemCat3.getName();//取出分类对象中name
        ItemCat itemCat2 = itemCatDao.selectByPrimaryKey(category2Id);
        String itemCat2Name = itemCat2.getName();
        ItemCat itemCat1 = itemCatDao.selectByPrimaryKey(category1Id);
        String itemCat1Name = itemCat1.getName();
        map.put("itemCat3",itemCat3Name);
        map.put("itemCat2",itemCat2Name);
        map.put("itemCat1",itemCat1Name);


//        ------------------------------------------
//        6.声明一个输出流
        Writer writer = null;
        try {
//        2.获取 指定的 一个模板对象:复制成品页面到web-inf下
            Template template = configuration.getTemplate("item.ftl");
//        3.文件输出路径 全路径//io 存,需要配置编码 字节流能配置编码集
            writer = new OutputStreamWriter(new FileOutputStream(realPath),"UTF-8");
            template.process(map,writer);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (null!=writer){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    写一个获取相对路径的方法
    public String getRealPath(String str){
        return servletContext.getRealPath(str);
    }

//  在这个类中声明一个ServletContext 接收传递进来的变量
    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
//        接一下,传递
        this.servletContext = servletContext;
    }
}
