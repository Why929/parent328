package cn.itcast.core.listener;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemDeleteListener implements MessageListener {
    @Autowired
    SolrTemplate solrTemplate;
    @Override
    public void onMessage(Message message) {
//      1.转换接收消息类型
        ActiveMQTextMessage goods_id_container = (ActiveMQTextMessage) message;
//      2.取出信息
        try {
            String goods_id = goods_id_container.getText();
//            3.删除solr索引库中的信息
            //            删除solr索引库中 item对象中good_id = id 的所有数据
            /**
             * 关于Criteria到底是什么:(到哪个域取查询).xx(查询哪个关键词)
             * 复制域 定义域 区别:复制域 中包含了普通域中的 部分字段域存储的数据,将这些信息放在一起 起一个新的域名,
             * 所以现在solr索引库中:有 1 2 3 4 5 6 7 8 号域
             *                      9复制域(2 3 5 7 9)
             *                      所以你 查询/删除 可以到 分域去做
             *                                      也可以到总域取做
             */
            Criteria criteria = new Criteria("item_goodsid").is(goods_id);
            SimpleQuery solrDataQuery = new SimpleQuery(criteria);
            solrTemplate.delete(solrDataQuery);
//          solr索引库的修改,一定要 提交事务
            solrTemplate.commit();
//            todo:删除item库中goods_id = id 的状态码 status = 0
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
