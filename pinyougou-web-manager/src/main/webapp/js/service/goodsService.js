//服务层
app.service('goodsService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../goods/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../goods/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../goods/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../goods/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../goods/update.do',entity );
	}

	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../goods/search.do?page='+page+"&rows="+rows, searchEntity);
	}

    //删除:商品审核页:功能:goods.html  ids:复选框批量id信息   status:用户绝对审批的状态码
    this.dele=function(ids){
        return $http.get('../goods/delete.do?ids='+ids);
    }
	//审批:商品审核页:功能:goods.html  ids:复选框批量id信息   status:用户绝对审批的状态码
	this.updateStatus = function(ids,status){
		return $http.get('../goods/updateStatus.do?ids='+ids+"&status="+status);
	}
});
