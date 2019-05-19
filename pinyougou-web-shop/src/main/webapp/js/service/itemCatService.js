//服务层
app.service('itemCatService',function($http){

	//查询分类表中所有分类:findItemCatList()
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../itemCat/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../itemCat/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体//坑
    /**
	 * 坑:这个现在是在Web-shop项目中,而现在调用的itemCat则曾经出现在Web-manager项目中
	 * 而这个itemCatService.js则是同样出现在了上述两个项目中了,
     * @param id
     */

	this.findOne=function(id){
		return $http.get('../itemCat/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../itemCat/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../itemCat/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../itemCat/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../itemCat/search.do?page='+page+"&rows="+rows, searchEntity);
	}    	

	//查询 一级/二级/三级分类:
	this.findByParentId = function(parentId){
		return $http.get("../itemCat/findByParentId.do?parentId="+parentId);
	}
});
