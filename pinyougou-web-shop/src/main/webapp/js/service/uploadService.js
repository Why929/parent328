app.service("uploadService",function($http){


	//上传图片最终追踪到这里:
	this.uploadFile = function(){
		// 向后台传递数据:
		var formData = new FormData();
		// 向formData中添加数据:
        /**.
		 * 向form表单中追加 子标签:Key : value
		 * input id = file    Key
		 * angularJs 取图片变量:file.files[0]; value
		 * .files[index]:是如果开启了 Multipart :多张上传属性,则可以使用这个:遍历
         */
		formData.append("file",file.files[0]);//todo:多个时  怎么上传?怎么接收?
		
		return $http({
			method:'post',
			url:'../upload/uploadFile.do',
			data:formData,
			headers:{'Content-Type':undefined} ,// Content-Type : text/html  text/plain  :MultipartFile/formDate
			transformRequest: angular.identity  //angularJS异步请求
		});
	}
	
});