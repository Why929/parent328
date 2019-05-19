package cn.itcast.core.service;

import cn.itcast.core.mapper.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
//连接两个数据库,这个还好使,但是连接3个数据库,就必须使用分布式事务
//实质是：在执行方法时，先执行切面dataSourceTransactionManager 向MySql 发送begin,结束发送commit,异常 发送rollback
public class ContentServiceImpl implements ContentService {
	
	@Autowired
	private ContentDao contentDao;

	@Override
	public List<Content> findAll() {
		List<Content> list = contentDao.selectByExample(null);
		return list;
	}

	@Override
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void add(Content content) {
		contentDao.insertSelective(content);
	}
//编辑修改轮播图 存在Map中  轮播图1:value1  轮播图2:value2
	@Override
	public void edit(Content content) {
//		1.根据content_id查询MySql中没有修改之前的content_categoryId
		Content contentOrg = contentDao.selectByPrimaryKey(content.getId());
//		2.优化:解决事务问题:导致的MySql虽然Rollback了,但是redis执行了delete,所以把Mysql更新提到第二步
		contentDao.updateByPrimaryKeySelective(content);
//		2.判断 有没有修改广告类型:categoryId:对比页面传递过来的categoryId与MySql中传递过来的categoryId
		if (!(contentOrg.getCategoryId()==content.getCategoryId())){
//			3.如果页面传递的categoryId != 更新前MySql查询到CategoryId,则说明 更改了广告分类
//			  那么需要1.删除categoryId1缓存:MySql中原来的那个CategoryId 2.删除categoryId2缓存:页面传递过来的categoryId
			redisTemplate.boundHashOps("content").delete(content.getCategoryId());
		}
//		4.不管 改没改 categoryId分类,都删除原来的 categoryId在redis中的缓存
		redisTemplate.boundHashOps("content").delete(contentOrg.getCategoryId());
//		替用户更新轮播图缓存
		findByCategoryId(content.getCategoryId());
		findByCategoryId(contentOrg.getCategoryId());
	}

	@Override
	public Content findOne(Long id) {
		Content content = contentDao.selectByPrimaryKey(id);
		return content;
	}

	@Override
	public void delAll(Long[] ids) {
		if(ids != null){
			for(Long id : ids){
				contentDao.deleteByPrimaryKey(id);
			}
		}
	}

//	注入RedisTemplate,需要将RedisTemplate配置文件导入到Common项目中的resources文件夹下,因为好多项目都要使用redis缓存
	@Autowired
	RedisTemplate redisTemplate;
//商城首页:广告轮播图:根据categoryId 查找tb_content表中的 的结果集List<Content>,Content:title url pic status
	@Override
	public List<Content> findByCategoryId(Long categoryId) {
//		使用redisTemplate，需要在上面注入RedisTemplate
//		1.首先到redis中根据id到redis中指定content区域查询目标集合,看看有没有,如果没有到Mysql中查询一份
		List<Content> contentList = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
//		2.判断查到没有:优化:1.先判断是不是==空,||(或者)size==0,是,则到数据库查询,赋值给上面contentList,再放到Redis中一份,然后在if外层放一个return
		if (null == contentList || contentList.size()==0){
//			1.到Mysql中去查询:创建Content查询对象
			ContentQuery contentQuery = new ContentQuery();
//			2.完善查询对象条件:
			ContentQuery.Criteria criteria = contentQuery.createCriteria();
			criteria.andCategoryIdEqualTo(categoryId);
//			3.执行查询语句,然后将查询结果赋值给 上面的contentList
			contentList = contentDao.selectByExample(contentQuery);
//			4.然后保存到Redis中一份:key:categoryId value:contentList
			redisTemplate.boundHashOps("content").put(categoryId,contentList);
//			5.配置广告存活时间:1.通过redis配置 2.通过定时器配置,这里是用1.通过redis配置
			redisTemplate.boundHashOps("content").expire(1, TimeUnit.MINUTES);
		}
//		3.返回ContentList
		return contentList;



		/* 没有用redis的时候
//		categoryId是content表中的外键:所以使用查询对象
		ContentQuery contentQuery = new ContentQuery();
		ContentQuery.Criteria criteria = contentQuery.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
//		排序
		contentQuery.setOrderByClause("sort_order desc");
//		执行操作
		List<Content> contentList = contentDao.selectByExample(contentQuery);
		return contentList;


		 */


	}

}
