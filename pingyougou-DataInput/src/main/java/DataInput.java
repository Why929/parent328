import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;
//什么东西
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")//加载Dao.xml
public class DataInput {
    @Autowired
    SolrTemplate solrTemplate;
    @Autowired
    ItemDao itemDao;

    /**
     * 将mysql中Items表的数据都存到solr索引库中
     * 要点:Items表中有个一个规格字段 spec,这个字段中[的数据数量] 不固定,
     * 所以,从Mysql取出来Spec 集合数据后List<Item>,要想存到Solr索引库中,
     * 需要先将 Item 对象中的spec属性中的值,放到specMap属性中
     */
    @Test
    public void add(){
//        1.导入Item表所有数据
        List<Item> items = itemDao.selectByExample(null);
        System.out.println(items);
//        2.存储前,先将数据中的规格spec更改Json串更改为 Map
        for (Item item : items) {
//           3.放回去            2.转换             1.取出来 {"包装":"5瓶","酒精度":"55度","容量":"2500ml"} json串
            item.setSpecMap(JSON.parseObject(item.getSpec(),Map.class));
        }
//        3.存入Solr索引库
        solrTemplate.saveBeans(items);
        solrTemplate.commit();


    }
}
