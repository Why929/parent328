//服务层
app.service('itemCatService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../itemCat/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../itemCat/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
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
    //页面初始化:先走这里:当点击 商品分类管理的时候,会查询 所有一级本类 信息回显:根据父id = 0;
	//根据一级分类id查询 所以一级分类信息:这是一条公共方法:查询二级分类就是传递二级分类id:
	//查询三级分类参数就是三级分类id:
	this.findByParentId = function(parentId){
		return $http.get("../itemCat/findByParentId.do?parentId="+parentId);
	}
});
