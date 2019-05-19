package cn.itcast.core.service;

import cn.itcast.core.mapper.good.BrandDao;
import cn.itcast.core.mapper.good.GoodsDao;
import cn.itcast.core.mapper.good.GoodsDescDao;
import cn.itcast.core.mapper.item.ItemCatDao;
import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.mapper.seller.SellerDao;
import cn.itcast.core.pojo.good.*;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemCat;

import cn.itcast.core.pojo.item.ItemQuery;
import cn.itcast.core.pojo.seller.Seller;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import pojogroup.GoodsVo;

import javax.jms.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
@SuppressWarnings("all")
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsDao goodsDao;//保存第一张表
    @Autowired
    private GoodsDescDao goodsDescDao;//保存第二张表
    @Autowired
    private ItemDao itemDao;//保存第三张表
    @Autowired
    private ItemCatDao itemCatDao;//根据第一张表中的Category3Id(),到ItemCat表中查对应的名称
    @Autowired
    private SellerDao sellerDao;//根据第一张表中的SellerId(),到Seller表中查询 商家名称
    @Autowired
    private BrandDao brandDao;//根据第一张表中的BrandId(),到Brand表中查询品牌名称
    @Autowired
    private Destination topicPageAndSolrDestination;//topic 会话组一
    @Autowired
    private Destination delItemSolrAndGoodsIsDelDestination;
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private SolrTemplate solrTemplate;
//    将生成模板功能的 方法,封装到一个类中,然后注入进来
//    1.新建一个接口 StaticPageService
//    2.在service-sellergoods中建其实现类
//    3.回来到这里调用


    //保存三张表
    @Override
    public void add(GoodsVo vo) {
//        -------------------保存第一张表-------------------
//      1.商品表返回ID ->到goodsDao xml中更改 insertSelective 相应标签
//      2.审核状态:手动配置:默认新增商品 状态为 0
        vo.getGoods().setAuditStatus("0");
//      3.至此保存第一张表:何时执行保存?到数据库中 对 第一张表,直到把缺省的都补全
        goodsDao.insertSelective(vo.getGoods());
//        --------------------保存第二张表-------------------
//      1.统一第二张表中的 GoodsId=第一张表中的id
        vo.getGoodsDesc().setGoodsId(vo.getGoods().getId());
//      2. 至此保存第二张表:
        goodsDescDao.insertSelective(vo.getGoodsDesc());
//        --------------------保存第三张表-------------------
//        判断是否启用第三张表
        if ("1".equals(vo.getGoods().getIsEnableSpec())){
            //        1.库存表 每次保存的是一个List<Item>,每个Item是条规格分析:一条库存信息
//          而每一条Item中都有需要 "完善"的信息:手动完善
//        拿到List<Item>
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {
//            1.完善title属性:标题 = SPU名称 + Sku名称
//            1.拿到商品名:
                String title = vo.getGoods().getGoodsName();
//            2.拿到sku名称:spec 分类属性 中的value,item表中就有 简化版本
//            表中存储格式:{"机身内存":"16G","网络":"联通3G"}
//            思路:先拿到字符串,然后解析成Map,然后遍历成Set<Map.Entry<>>
                String spec = item.getSpec();
//            3.将Json格式字符串解析成Map对象
                Map<String,String> map = JSON.parseObject(spec, Map.class);
//            4.将Map转成Entry对象
                Set<Map.Entry<String, String>> entries = map.entrySet();
//            5.循环取出每个映射关系中的value:{"机身内存":"16G","网络":"联通3G"}
                for (Map.Entry<String, String> entry : entries) {
//            6.取出value值,拼接到title中
                    title += " " + entry.getValue();
                }
//            7.至此title拼接完毕,补全到item对象中
                item.setTitle(title);
//            8.保存商品第一张图片:作为日后页面首页使用的:大字段:在GoodsDesc表中
//[{"color":"粉色","url":"http:04.jpg"},{"color":"黑色","url":"http:7.jpg"}]
                String itemImages = vo.getGoodsDesc().getItemImages();
//            9.解析Json格式itemImages对象成Map
                List<Map> itemImagesList = JSON.parseArray(itemImages, Map.class);
//            10.判断集合是不是为空 && .size>0
                if (null != itemImagesList && itemImagesList.size() > 0){
//            11.如果不为空,则取出来第一张图片地址
                    item.setImage((String) itemImagesList.get(0).get("url"));
                }
//            12.第三个商品分类ID:解释:添加商品时:具体添加到了三级分类下:所以添加的每条商品都要加上 3级分类
                item.setCategoryid(vo.getGoods().getCategory3Id());
//            13.第三个商品分类的名称:新增商品仍然需要 三级分类id
//               需要根据3级分类id到 :itemCat 表中取查找.itemCat表是商品分类表
                ItemCat itemCat = itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id());
//            14.使用itemCat对象获得 三级分类名称,补全到item中
                item.setCategory(itemCat.getName());
//            15.//补全时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
//            16.统一三张表 id 建立统一对应关系:商品表的id
                item.setGoodsId(vo.getGoods().getId());
//            17.商家Id:注意:要点:商家Id是一个特殊字段:是String类型串,在Seller表中是主键:商品表存的是SelllerId,这个ID在Seller有具体对应的Name
                item.setSellerId(vo.getGoods().getSellerId());
//            18.补全商家名称:即新增商品的 所属商家信息,商家信息在Seller表中,商品表中只存了SellerId,需要根据这个特殊id查询其对应的Name
                Seller seller = sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId());
//               使用Seller对象,拿到名字,补全item
                item.setSeller(seller.getName());
//            19.品牌名称:页面提供的虽然是StringChinese 但底层使用的是 item.id  as  item.text,会自动进行 id与texe别名映射,但最后使用的还是id,所以要使用具体名称,还得手动根据id去查询
                Brand brand = brandDao.selectByPrimaryKey(vo.getGoods().getBrandId());
//               使用brand对象获取id,补全item
                item.setBrand(brand.getName());
//            20.至此,单条item新增库存信息补全完毕,指定插入操作:使用.insertSelective :可以插入信息不全的item对象而不报错
                itemDao.insertSelective(item);
            }
        }else {
            // TODO: 不启用item规格表,但也要保存,需要给指定一个默认值,之后来写
        }
    }


//分页查询:
//    同时有两个 访问对象:运营商+商家后台
//    判断标准就是:有没有传递  SellerId:当前登录用户id
//    给 goods.getSellerId()进行判断,然后判断是不是需要进行拼接criteria.andSellerIdEqualTo()
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
//        1.分页插件首先配置了:将前台传递过来的:当前页+每页显示条数
        PageHelper.startPage(page,rows);
//        2.使用分页功能的其他功能
        PageHelper.orderBy("id desc");
//        3.创建条件查询对象
        GoodsQuery goodsQuery = new GoodsQuery();
//        4.取出条件对象
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
//        5.判断条件查询 的条件都有哪些
//        5.1判断 复选框查询条件选择是不是:查询全部,如果不是查询全部则 拼接具体的条件查询 条件
        if (null != goods.getAuditStatus() && !"".equals(goods.getAuditStatus().trim())){
//            默认查询条件为:AuditStatus = "",如果不满足这个条件就是查询所有,那么拼接补全具体 条件
            criteria.andAuditStatusEqualTo(goods.getAuditStatus());
        }
//        6. 判断输入框 是否有具体 条件输入,有则判断
        if (null != goods.getGoodsName() && !"".equals(goods.getGoodsName().trim())){
//            如果有输入条件:则进行条件 拼接补全:模糊查询要点:需要手动 给条件拼接 "%"
            criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
        }
//        7.条件拼接:被删除的商品,不查询
        criteria.andIsDeleteIsNull();
//        8.条件拼接:只查询当前商家的商品_来自商家 还是 查询所有_来自运营商
        if (null!=goods.getId()){
//            如果来自于商家后台的查询则,进行查询
            criteria.andSellerIdEqualTo(goods.getSellerId());
        }

//        9.进行查询:技巧:直接返回Page<Goods> 对象:Page是导PageHelper是直接导入的
        Page<Goods> p = (Page<Goods>)goodsDao.selectByExample(goodsQuery);
//        10.返回一个PageResult对象:总条数+结果集
        PageResult pageResult = new PageResult(p.getTotal(), p.getResult());
        return pageResult;
    }

//    商品修改之 信息回显 根据id查询商品
    @Override
    public GoodsVo findOne(Long id) {
//        创建一个返回对象
        GoodsVo goodsVo = new GoodsVo();
//        1.根据Id查询商品表
        Goods goods = goodsDao.selectByPrimaryKey(id);
//        补全Vo
        goodsVo.setGoods(goods);
//        2.根据id查询商品详情表
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
//        补全Vo
        goodsVo.setGoodsDesc(goodsDesc);
//        3.根据id查询库存表:结果集
//        要点:库存表中id是一个外键:所以需要创建查询对象进行查询:将查询条件放到对象中,使用对象进行查询
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
//        补全查询条件:=id的信息
        criteria.andGoodsIdEqualTo(id);
//        执行查询
        List<Item> itemList = itemDao.selectByExample(itemQuery);
//        补全Vo
        goodsVo.setItemList(itemList);
        return goodsVo;
    }

//    商品修改按钮触发,信息回显修改之后,的保存操作
    @Override
    public void update(GoodsVo vo) {
//      1.更新商品表
        goodsDao.updateByPrimaryKeySelective(vo.getGoods());
//      2.更新商品详情表
        goodsDescDao.updateByPrimaryKeySelective(vo.getGoodsDesc());
//      3.库存表:规格:
//         3.1 先删除
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(vo.getGoods().getId());
        itemDao.deleteByExample(itemQuery);
//         3.2 在插入
//        先判断有没有规格选项
        //        --------------------保存第三张表-------------------
//        判断是否启用第三张表
        if ("1".equals(vo.getGoods().getIsEnableSpec())){
            //        1.库存表 每次保存的是一个List<Item>,每个Item是条规格分析:一条库存信息
//          而每一条Item中都有需要 "完善"的信息:手动完善
//        拿到List<Item>
            List<Item> itemList = vo.getItemList();
            for (Item item : itemList) {
//            1.完善title属性:标题 = SPU名称 + Sku名称
//            1.拿到商品名:
                String title = vo.getGoods().getGoodsName();
//            2.拿到sku名称:spec 分类属性 中的value,item表中就有 简化版本
//            表中存储格式:{"机身内存":"16G","网络":"联通3G"}
//            思路:先拿到字符串,然后解析成Map,然后遍历成Set<Map.Entry<>>
                String spec = item.getSpec();
//            3.将Json格式字符串解析成Map对象
                Map<String,String> map = JSON.parseObject(spec, Map.class);
//            4.将Map转成Entry对象
                Set<Map.Entry<String, String>> entries = map.entrySet();
//            5.循环取出每个映射关系中的value:{"机身内存":"16G","网络":"联通3G"}
                for (Map.Entry<String, String> entry : entries) {
//            6.取出value值,拼接到title中
                    title += " " + entry.getValue();
                }
//            7.至此title拼接完毕,补全到item对象中
                item.setTitle(title);
//            8.保存商品第一张图片:作为日后页面首页使用的:大字段:在GoodsDesc表中
//[{"color":"粉色","url":"http:04.jpg"},{"color":"黑色","url":"http:7.jpg"}]
                String itemImages = vo.getGoodsDesc().getItemImages();
//            9.解析Json格式itemImages对象成Map
                List<Map> itemImagesList = JSON.parseArray(itemImages, Map.class);
//            10.判断集合是不是为空 && .size>0
                if (null != itemImagesList && itemImagesList.size() > 0){
//            11.如果不为空,则取出来第一张图片地址
                    item.setImage((String) itemImagesList.get(0).get("url"));
                }
//            12.第三个商品分类ID:解释:添加商品时:具体添加到了三级分类下:所以添加的每条商品都要加上 3级分类
                item.setCategoryid(vo.getGoods().getCategory3Id());
//            13.第三个商品分类的名称:新增商品仍然需要 三级分类id
//               需要根据3级分类id到 :itemCat 表中取查找.itemCat表是商品分类表
                ItemCat itemCat = itemCatDao.selectByPrimaryKey(vo.getGoods().getCategory3Id());
//            14.使用itemCat对象获得 三级分类名称,补全到item中
                item.setCategory(itemCat.getName());
//            15.//补全时间
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
//            16.统一三张表 id 建立统一对应关系:商品表的id
                item.setGoodsId(vo.getGoods().getId());
//            17.商家Id:注意:要点:商家Id是一个特殊字段:是String类型串,在Seller表中是主键:商品表存的是SelllerId,这个ID在Seller有具体对应的Name
                item.setSellerId(vo.getGoods().getSellerId());
//            18.补全商家名称:即新增商品的 所属商家信息,商家信息在Seller表中,商品表中只存了SellerId,需要根据这个特殊id查询其对应的Name
                Seller seller = sellerDao.selectByPrimaryKey(vo.getGoods().getSellerId());
//               使用Seller对象,拿到名字,补全item
                item.setSeller(seller.getName());
//            19.品牌名称:页面提供的虽然是StringChinese 但底层使用的是 item.id  as  item.text,会自动进行 id与texe别名映射,但最后使用的还是id,所以要使用具体名称,还得手动根据id去查询
                Brand brand = brandDao.selectByPrimaryKey(vo.getGoods().getBrandId());
//               使用brand对象获取id,补全item
                item.setBrand(brand.getName());
//            20.至此,单条item新增库存信息补全完毕,指定插入操作:使用.insertSelective :可以插入信息不全的item对象而不报错
                itemDao.insertSelective(item);
            }
        }else {
            // TODO: 不启用item规格表,但也要保存,需要给指定一个默认值,之后来写
        }
    }

//    商品审核:删除:逻辑删除: 更改isDelete 状态为 1
//    由于没有 商品管理 页面选项,所以演示 索引库删除/更新 就在这里进行测试了

    /**
     * 要点::逻辑删除goods表中delete状态码
     * 如果该商品的 最小产品单位被删除了 spu信息被删除了,那么相应solr索引库中所有 =该good_id的 sku信息item信息都应该被移出
     * 索引库->移出 item中good_id = ids 的所有数据,另外:item 数据库中的状态码也应该更改成 1
     * @param ids
     */
    @Override
    public void delete(Long[] ids) {
//        ----------------删除:1.将goods表中的status状态码配置成1在这里 2.将solr索引库中的记录删除:传来的是goodis 删除的是item,跟search相关移到search中 ----
//        1.创建一个Goods对象 供  ....ByExampleSelective(arg)来使用
        Goods goods = new Goods();
//        2.配置 删除状态码:为删除状态:"1"
        goods.setIsDelete("1");//删除状态码 媒介配置完毕
//        3.批量删除多个商品:在数组中,需要遍历
        for (Long id : ids) {//要不要判断ids为空
//      updateByPrimaryKeySelective()      selectByExample()
//      一个使用的是Goods对象              一个使用的是GoodsQuery对象   :这是一个区别
//            4.确定要删除的是哪个对象
            goods.setId(id);
//            5.执行逻辑删除:id是主键,所以要根据主键进行删除
            goodsDao.updateByPrimaryKeySelective(goods);
//            -----------------------发送jms---------------------------------------
            //            删除solr索引库中 item对象中good_id = id 的所有数据
            /**
             * 关于Criteria到底是什么:(到哪个域取查询).xx(查询哪个关键词)
             * 复制域 定义域 区别:复制域 中包含了普通域中的 部分字段域存储的数据,将这些信息放在一起 起一个新的域名,
             * 所以现在solr索引库中:有 1 2 3 4 5 6 7 8 号域
             *                      9复制域(2 3 5 7 9)
             *                      所以你 查询/删除 可以到 分域去做
             *                                      也可以到总域取做
             */
//           6.向JMS发送信息:建立一个新的 会话名,在xml中实例化
            jmsTemplate.send(delItemSolrAndGoodsIsDelDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {

                    TextMessage idMq = session.createTextMessage(String.valueOf(id));
                    return idMq;
                }
            });
        }
//        --------------------------------------------------
    }

//    商品审核:同意/不同意 ,由于没有 商品管理 页面选项,所以演示 索引库删除/更新 就在这里进行测试了
    /**
     * 要点:审核修改的是 spu表 : goods表
     * 添加索引库的是 sku表   : item表 具体型号 每个都有多少 库存表
     */

  @Override
    public void updateStatus(Long[] ids, String status) {
//      创建Goods对象____修改某一个条数据的审核状态,需要借助对象来进行修改:修改的是goods spu表
        Goods goods = new Goods();
//      配置AuditStatic状态:传递过来的需要修改的对象
        goods.setAuditStatus(status);
//      遍历ids:有可能是批量修改 一堆数据的状态值
        for (Long id : ids) {
//          1.配置Goods id
            goods.setId(id);
//          2.同意/不同意  都需要更新数据:执行一条数据更新操作:更新:审核状态码
            goodsDao.updateByPrimaryKeySelective(goods);
//          3.同意的时候:需要将商品添加到:索引库 添加的是item sku库存表
            if ("1".equals(status)){//如果同意审核通过
//                1.发送消息(                        目标对象_xml中配置  ,             消息发送器  )

                try {
                    jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
    //                      2.消息承载容器:传递什么消息内容?
    //                              id,审核通过了这个商品,需药:1.item到solr索引库
    //                                                       2.生成静态页面:商品详情页面回显:
    //                                                         从Mysql中,redis中的是搜索之后关键词 回显商品集合
    //                        要点:注意其传递的是五大数据类型:一个字符串对象,一套名称,一个序列化的java对象,一个字节的数据流,java原始值的数据流
    //                        这里要传递的id 是Long类型的,所以,这里可以转成字符串
                            TextMessage textMessage = session.createTextMessage(String.valueOf(id));
//                            session.close();//session需要关流
                            return textMessage;//匿名对象,返回消息传递承载容器
    //                        将id发送到JMS上一个会话后.该会话的接收成员->处理商品审核通过后的:1.添加到solr索引 2.生成该商品的静态化页面
    //                        所以该会话中的两个接收成员,B C 分别监听JMS,发现有新的id传递进来,分别取id,各自进行 相关业务的处理
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("jmsTemplate.sent处的异常");
                }

                // 1.客户 喂 人家


//          *****使用JMS消息中间件 通知service-search项目,这个id的商品sku需要添加到solr索引库****************
//                4.todo:保存商品信息到索引库:添加的是 sku item表信息: 与goods表的对应关系: item表中有goods_id 属性
//                问题:根据是添加到 索引库中吗?不添加到缓存库中么?
//                答:现在是广告轮播图放到了索引库中,其他的还没有放,现在只是将数据库中 已经存在的数据库的 一个状态值修改了一下
//                1.添加到索引库:问题:添加的是什么?都添加什么?
//                答:改的是:回显结果集+回显表头
//                添加什么?
//                答:修改了哪一条数据,就向solr索引库中 添加哪一条数据,修改了一套item数据,那么就向solr索引库中添加一条 item数据,注意修改spec ->specMap

//                1.根据id查询mysql中item表中的goods_id, spu表需要审核,sku表也需要审核,但是,现在没有sku表的审核页面,
//                  所以现在,就是手动选择添加item_status属性 = 1 的商品库存信息到 solr索引库


//----------------------------------------------------------------------------------------------------------------------
//          *****使用JMS消息中间件 通知service-Page项目,这个id的商品需要生成一个静态化页面****************
//                解析:这个静态化页面,里面回显了该id商品的 各种规格信息,当点击购物的时候,会再次将 购买者选择的参数,传递到后台
//   5.todo:静态化页面:静态化页面要怎么处理?
//                1.当商品审核通过的时候:添加到solr索引库之后,
//                当用户 点击某具体商品的时候,跳转到 该商品的商品详情页面
//                这个商品详情页面,是有java 通过 freemaker 使用模板 生成的 静态化页面,
//                整体流程:审核通过 Goods spu后,item sku审核通过后,生成商品详情 静态页面 :goodsDesc item
//                1.springmvc 包中有对freemaker的支持
//                staticPageService.index(id);

            }

        }
    }
}


