// 定义服务层:
/**
 * 要点一:分离出来的,需要别引用回去
 * 要点二:需要在xx.html中引用,不然不会生效
 * 要点三:$http.get("../    都加了 ../ 即返回上一级
 * 		原因:页面和java都在同一个项目中
 * 		而页面访问->WebApp同级目录java根目录的层级包中的Controller.java文件
 * 		所以需要:访问WebApp同级别下的Controller中的xxx
 * 			由此可知:Controller在java下不影响访问:直接访问到WebApp级别就能访问到:controller中的相应方法
 */

app.service("brandService",function($http){
	this.findAll = function(){
		return $http.get("../brand/findAll.do");
	}
	
	this.findPage = function(page,rows){
		return $http.get("../brand/findPage.do?pageNum="+page+"&pageSize="+rows);
	}
	
	this.add = function(entity){
		return $http.post("../brand/add.do",entity);
	}
	
	this.update=function(entity){
		return $http.post("../brand/update.do",entity);
	}
	
	this.findOne=function(id){
		return $http.get("../brand/findOne.do?id="+id);
	}
	
	this.dele = function(ids){
		return $http.get("../brand/delete.do?ids="+ids);
	}
	
	this.search = function(page,rows,searchEntity){
		return $http.post("../brand/search.do?pageNum="+page+"&pageSize="+rows,searchEntity);
	}
	
	this.selectOptionList = function(){
        /**
		 * 追踪findBrandList()到这里,需要到后台去实现,selectOptionList,需要返回[{},{},{}] 因为目标表中就是要求这样存贮的,使用List<Map>实现:要到brandController中去实现
         */
		return $http.get("../brand/selectOptionList.do");
	}
});