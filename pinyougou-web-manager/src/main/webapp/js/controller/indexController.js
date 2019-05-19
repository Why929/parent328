app.controller("indexController",function($scope,loginService){
	//追踪showName()到这里:发现返回值:loginName域变量 index.html中有{{loginName}}获取域值
	//和{{cur_time}}当前时间
	//那么response:是一个Map<String,Object>
	//					loginNme:小白
	//                  cur_time:2019年3月2日21:15:01
	//继续追踪loginService.js 查看访问的后台controller
	$scope.showName = function(){
		loginService.showName().success(function(response){
			$scope.loginName = response.loginName;
			$scope.cur_time = response.cur_time;
		});
	}
	
});