package cn.itcast.core.service;

import cn.itcast.core.pojo.ad.Content;
import entity.PageResult;

import java.util.List;

public interface ContentService {

	public List<Content> findAll();
	
	public PageResult findPage(Content content, Integer pageNum, Integer pageSize);
	
	public void add(Content content);
	
	public void edit(Content content);
	
	public Content findOne(Long id);
	
	public void delAll(Long[] ids);

	//根据categoryId 查找content表中的 的结果集
	List<Content> findByCategoryId(Long categoryId);
}
