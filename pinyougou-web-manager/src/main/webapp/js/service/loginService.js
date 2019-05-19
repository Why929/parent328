app.service("loginService",function($http){
	// 追踪showName到这里:确定是了controller的名
	//和上一级的返回参数类型:Map<Spring,Object>
	//然后去后台:创建controller
	this.showName = function(){
		return $http.get("../login/showName.do");
	}
	
});