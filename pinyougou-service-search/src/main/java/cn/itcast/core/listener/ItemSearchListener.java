package cn.itcast.core.listener;

import cn.itcast.core.mapper.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

//JMS 消费方
@Service
public class ItemSearchListener implements MessageListener {
    @Autowired
    ItemDao itemDao;//据说这个错,没鸟关系
    @Autowired
    SolrTemplate solrTemplate;
    @Override
    public void onMessage(Message message) {
//        1.接收消息对象,进行转换:转成五大数据类型之一:TextMessage MapMssage ObjectMessage BytesMessage StreamMessage
        ActiveMQTextMessage addItem2SolrTextMessage = (ActiveMQTextMessage) message;
//        2.取出值
        try {
            String id = addItem2SolrTextMessage.getText();
            System.out.println("添加索引库项目收到的id->"+id);
//            3.根据消息值id处理业务
//            --------------------------
            ItemQuery itemQuery = new ItemQuery();
            ItemQuery.Criteria criteria = itemQuery.createCriteria();
            criteria.andGoodsIdEqualTo(Long.parseLong(id)).andStatusEqualTo("1");//手动选择=1
//               查询库存表中id=goods 的库存商品添加到 solr索引库
            List<Item> items = itemDao.selectByExample(itemQuery);
//                更改item spec ->specMap
            for (Item item : items) {
                item.setSpecMap(JSON.parseObject(item.getSpec(),Map.class));
            }
            solrTemplate.saveBeans(items);
            solrTemplate.commit();
//            没有提交也不影响啊
//            --------------------------
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
