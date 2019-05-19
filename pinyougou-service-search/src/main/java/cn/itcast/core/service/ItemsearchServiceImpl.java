package cn.itcast.core.service;


import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/**
 * 搜索商品,就不是从Mysql中查询了,是从solr索引库中查询了
 */

@Service
public class ItemsearchServiceImpl implements ItemsearchService {

    @Autowired
    SolrTemplate solrTemplate;
    @Autowired
    RedisTemplate redisTemplate;

    /*
    向后端传递一个Map变量,每个搜索条件都存在 如下key中
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
     */
    @Override
    public Map<String, Object> search(Map<String,String> searchMap) {
        /**
         * 处理searchMap keywords 中字符串中包含 空格,导致 solr索引库分词后,在item_keywords复合域中查询,查询不到相应关键字
         *     其他域名都是写死的,
         */
//                        3.再放回去                1.取出来                    2.替换
        searchMap.put("keywords",searchMap.get("keywords").replace(" ",""));

        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        //新建返回值 对象Map
        Map<String, Object> resultMap = new HashMap<>();
        //1.商品分类:根据关键字:回显商品分类信息列表
//  ---------回显关键词 对应第一个分类下的所有商品信息-------
        List<String> categoryList = searchCategoryByKeywords(searchMap);
        resultMap.put("categoryList",categoryList);//放到回显页面变量中

//        ------------------回显:品牌 规格 方法--------------------
        //2.品牌  //从redis中取 根据 列表中第一个分类(根据关键字:回显商品分类信息列表) 到redis中查(itemCat) 分类id 根据id 在到redis(brandList)中查品牌
        //3.规格  //从redis中取
        //要求:显示 回显商品分类信息列表 中第一个分类 对应的 品牌 + 规格 信息
        if (null != categoryList && categoryList.size() > 0){
            String firstCatName = categoryList.get(0);//获取列表中第一个 分类name
//        3.2根据第一个分类的name到redis itemCat中取查找其对应的id
            Long firstItemCatId = (Long)redisTemplate.boundHashOps("itemCat").get(firstCatName);
//        3.3根据第一个分类的id 到redis brandList 中查找其对应的分类nameList
//        [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}] --->所以取出来是List<Map>
            Set brandList1 = redisTemplate.boundHashOps("brandList").keys();//测试是不是有输出
            System.out.println(brandList1);//测试是不是有输出
            List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(firstItemCatId);
//        [{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":9,"text":"苹果"},{"id":4,"text":"小米"},规格选项map]
            List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(firstItemCatId);

            resultMap.put("brandList",brandList);
            resultMap.put("specList",specList);
        }

//        -----------------回显:关键字 对应的商品结果集 进行高亮处理---------------------
//     跳转过来:首次先搜索这里:首页页面打开默认搜索?没有做/ 页面传递过来某关键词 之后,回显商品结果集
//        所以再次进行 商品筛选的时候,需要在这里进行 是否有筛选条件的 判断
        //4.结果集 searchHighlightPage
        //5.总条数 searchHighlightPage
        HighlightPage<Item> hLMethod_E = searchHighlightPage(searchMap);
        //55.向Map<String,String> 存入 ,都放到哪里?到Search.html中 看
        resultMap.put("rows",hLMethod_E.getContent());//商品信息结果
        resultMap.put("totalPages",hLMethod_E.getTotalPages());//总页数
        resultMap.put("total",hLMethod_E.getTotalElements());//总条数

        return resultMap;
    }
//    **************************根据关键字:回显商对应的 的所有分类 回显到分类 栏***************************************
    public List<String> searchCategoryByKeywords(Map<String,String> searchMap){
//        新建一个返回集合
        List<String> categoryList = new ArrayList<>();
        //        ---------------------入口+关键词------------------
//        1.根据关键词查询 solr索引库 keywords域中 的搜索关键词
//              1.查询域+查询关键字
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
//        2.中间容器
        Query query = new SimpleQuery(criteria);
        //        ---------------------分组 分组依据 域名------------------
//        3.分组关键域对象:根据哪个域分组
        GroupOptions groupOptions = new GroupOptions();
        GroupOptions item_categoryGroupOptions = groupOptions.addGroupByField("item_category");
//        4.装配分组对象
        query.setGroupOptions(item_categoryGroupOptions);
//        5.执行查询 + 返回结果封装
        GroupPage<Item> groupResult = solrTemplate.queryForGroupPage(query, Item.class);
        //        ---------------------取分组值:根据分组依据(域)取------------------
//        6.取出分组数据结果:配置的根据什么分组的 就拿什么取分组
        GroupResult<Item> item_category = groupResult.getGroupResult("item_category");
//        7.通过DeBug确定目标值 存在哪里
        Page<GroupEntry<Item>> groupEntries = item_category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        for (GroupEntry<Item> itemGroupEntry : content) {
//            8.取出分组后:组内每一个值
            String category = itemGroupEntry.getGroupValue();//最终值所在位置:GroupValue变量中
//            9.将值放到页面要求的 变量中:存在Map中的一个List<String_catgory>集合中:外侧new 一个集合
            categoryList.add(category);
        }
        System.out.println(item_category);//打断点 停止处
//        3.取出结果
        return categoryList;
    }
//  ****************************根据关键字:查询商品信息查询 是否高亮*************************************
    public HighlightPage<Item> searchHighlightPage(Map<String,String> searchMap){//其实也要判断这个searchMap 是不是为null 或''
//        ---------------------入口+关键词 q|*:*------------------
//                             定死+从searchMap中取出来_顶级搜索关键字
//                                  $scope.searchMap={'keywords':'顶级搜索关键字','category':'次级关键字_追加的','brand':'次级关键字_追加的','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//        2.查询所有符合条件参数:查询入口+关键字::到Solr哪个域取查询+查询关键字_顶级搜索关键字 配置
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//顶级查询词key 是固定的
//        3.高亮条件对象(args 查询所有符合条件参数)
        SimpleHighlightQuery highlightQuery = new SimpleHighlightQuery(criteria);//首次查询就到这里,到item_keywords(复合域中) 查询所有 包含keywords关键词的 结果集
//        **********------------highlightQuery:fq|+ + + + 搜索条件中:是否有_次级条件?:过滤条件-------*************
//        要判断是 1.首次查询 2.二次筛选查询
//        $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//        分类 品牌 价格  规格[] 等次级条件 是否存在判断:逐个判断
/*      要点分析:页面上 现有的分类栏中的 分类,是另一个method 根据关键词 单独查的 搜索关键词 对应分类List,然后单独回显到 页面的
                所以,用户点击 首次搜索 的时候,searchMap中是没有category 分类信息的
                只有 是条件搜索时,searchMap.category等次级条件才会 有所搜变量
*/
//      1.判断searchMap中有category这个变量吗? 取一下,判断不为null 或者 不等于 '' 么?
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category").trim())){
//          如果有这个category变量值
            //            筛选条件 item_category:  到哪个域进行继续搜索        :        搜索哪个关键词
            Criteria criteria1_cat = new Criteria("item_category").is(searchMap.get("category").trim());
            FilterQuery filterQuery = new SimpleQuery(criteria1_cat);
            highlightQuery.addFilterQuery(filterQuery);
        }
//      2.判断searchMap中有brand这个变量吗? 取一下,判断不为null 或者 不等于 '' 么?
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand").trim())){
//          如果有这个category变量值
//            筛选条件 brand:                    到哪个域进行继续搜索        :        搜索哪个关键词
            Criteria criteria1_cat = new Criteria("item_brand").is(searchMap.get("brand").trim());
            FilterQuery filterQuery = new SimpleQuery(criteria1_cat);
            highlightQuery.addFilterQuery(filterQuery);
        }
//      3.判断searchMap中有spec规格吗?这个变量吗? 取一下,判断不为null 或者 不等于 '' 么?
//        向后端传递一个Map变量,每个搜索条件都存在 如下key中
//        $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//                                                            筛选条件 spec:{到哪个域进行继续搜索:搜索哪个关键词
//  前端传递过来的searchMap第一层key value都是String,第二层嵌套是JsonString, {"item_spec_机身内存": "16G",
//                                                                       "item_spec_网络": "联通3G"}
//        所以关于这个规格spec中传递了几个规格对象,首先需要将Json串解析出来,然后才能取出来
        if (null != searchMap.get("spec") && !"".equals(searchMap.get("spec").trim())){
//          当传递过来的spec存在的是时候,取出其中的条件,拼接到 FilterQuery中,拼接到哪个字段域?
//          存的是动态域中,动态域是以item_spec_*动态存的,所以取的时候,域名前半段是item_spec_
            String specJsonString = searchMap.get("spec").trim();
            Map<String,String> specMap = JSON.parseObject(specJsonString, Map.class);
            Set<Map.Entry<String, String>> entries = specMap.entrySet();
//            变量取值

            for (Map.Entry<String, String> entry : entries) {
//               spec 中第一个 key value
                entry.getKey();
                entry.getValue();
//                拼接条件:1.筛选条件拼接:要在搜索结果出来后,继续以哪个域进行搜索,搜索哪个关键词
                Criteria criteria1 = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                SimpleQuery simpleQuery = new SimpleQuery(criteria1);
                highlightQuery.addFilterQuery(simpleQuery);
            }
        }
//      4.如果判断searchMap中有price这个变量吗? 取一下,判断不为null 或者 不等于 '' 么?
        if (null != searchMap.get("price") && !"".equals(searchMap.get("price").trim())){
//            价格种类可能 0-500  500-* ,所以首先要进行 字符串切割  springDataSolr 不支持 *,
//            1.先判断传递的变量中有没有 * String.contains("*")
//                  切割字符串向上提
            String[] priceArray = searchMap.get("price").split("-");
//                  向上提的
            SimpleFilterQuery simpleFilterQuery = new SimpleFilterQuery();
            if (!searchMap.get("price").contains("*")){
                //                        删选哪个域       :   搜索条件
                Criteria item_price = new Criteria("item_price").between(priceArray[0], priceArray[1], true, false);
                 simpleFilterQuery.addCriteria(item_price);

            }else {//如果包含*  ,则直接查询 <
                Criteria criteria_x = new Criteria("item_price").greaterThanEqual(priceArray[1]);
                simpleFilterQuery.addCriteria(criteria_x);
            }
//            3.判断完价格 区间种类 是什么样的之后,就添加到 过滤条件中
            highlightQuery.addFilterQuery(simpleFilterQuery);
        }
//         **********------------highlightQuery:sort|DESC ASC 追加排序条件 -------*************
//        排序使用的是import org.springframework.data.domain.Sort; 包,不是solr的包
//$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

        if (null != searchMap.get("sortField") && !"".equals(searchMap.get("sortField").trim())){
//            如果有排序域 :则判断是 升序还是 降序
//$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
//要点:前端存储变量名sortField:updatetime price price ,后端solr索引库中对应的 字段域 是不同的 要注意拼接补全
//不能进行sort 是否为空判断,因为排序是根据 sortFiled 是否存在来确定的,sort中升序/降序是用的是常量
//          默认:搜索结果是不带排序的

            if (null != searchMap.get("sort") && !"".equals(searchMap.get("sort").trim())){
                if ("DESC".equals(searchMap.get("sort"))){//如果是升序:
//                                               升序?     :  按照solr索引库中哪个域来 排序?    查看Item对象中怎么映射的@Field =>@Field("item_price")
                Sort  orders = new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField").trim());
                highlightQuery.addSort(orders);//排序条件拼接
                }else {//否则就是降序
                Sort  orders = new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField").trim());
                highlightQuery.addSort(orders);//排序条件拼接
                }
            }


        }


//        --------------------分页 start/rows--------------------------
        highlightQuery.setOffset((Integer.parseInt(searchMap.get("pageNo"))-1)*Integer.parseInt(searchMap.get("pageSize")));//分页偏移量
        highlightQuery.setRows(Integer.parseInt(searchMap.get("pageSize")));//分页没有显示条数
//        --------------------高亮:域+颜色 hl,有没有都要进行高亮配置,配置死了----------------------
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");//高亮显示哪个域             hl.fl
        highlightOptions.setSimplePrefix("<font style='color:red'>");//颜色 hl.simple.pre
        highlightOptions.setSimplePostfix("</font>");                //     hl.simple.post
        highlightQuery.setHighlightOptions(highlightOptions);


//        4.执行条件查询,以及 封装Pojo类型
        HighlightPage<Item> hLMethod_E = solrTemplate.queryForHighlightPage(highlightQuery, Item.class);
//        --------------------判断是否开启了高亮:取出高亮集合-->替换非高亮集合-----------
        List<HighlightEntry<Item>> highlighted = hLMethod_E.getHighlighted();
        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            Item entity = itemHighlightEntry.getEntity();//单条非高亮结果:与hLMethod_E.getContent()控制相同地址数据,双向绑定,一个变,另一个也变
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();//单条高亮结果
//            替换:首先判空:gighlights不为空,则替换
            if (null!=highlights && highlights.size()>0){
                String highlightedField = highlights.get(0).getSnipplets().get(0);
                entity.setTitle(highlightedField);
            }
        }
//        条件搜索:之排序: 查询结果:按照新旧排序:1970年旧------2019年新 按照这个时间轴进行新旧判断


        return hLMethod_E;
    }
}


