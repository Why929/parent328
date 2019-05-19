 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,typeTemplateService ,itemCatService,uploadService ,goodsService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//商品修改之 修改 按钮触发信息查询回显:根据Id
    //1.页面一加载就会初始化body中的两个方法,其中findOne()可以使用另外页面传递过来的id,
       // 新增商品:
      //  修改商品:两者都会跳转到这个页面,所以都会触发findOne()所以,需要在findOne()方法中进行判断,判断时候 有id参数传递过来,据此判断是不是需要执行findOne()方法
    //
	$scope.findOne=function(){
		//判断是不是修改触发的findOne:判断是不是有id参数传递过来
		//angularJs中 使用$location.search()["varName"]来接收,其他页面传递过来的参数
		var id = $location.search()['id'];
		if(null == id){
			return;
		}

		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;	
				
				// 调用处理富文本编辑器：
				editor.html($scope.entity.goodsDesc.introduction);
				
				// 处理图片列表，因为图片信息保存的是JSON的字符串，让前台识别为JSON格式对象:逆解析
				$scope.entity.goodsDesc.itemImages = JSON.parse( $scope.entity.goodsDesc.itemImages );
				
				// 处理扩展属性:
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse( $scope.entity.goodsDesc.customAttributeItems );
			
				// 处理规格
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
			
				// 遍历SKU的集合:
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec );
				}
			}
		);				
	}
	
	$scope.checkAttributeValue = function(specName,optionName){
		var items = $scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(items,"attributeName",specName);
		if(object != null){
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	//保存 
	$scope.save=function(){	
		// 再添加之前，获得富文本编辑器中的内容。
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.flag){
					//重新查询 
		        	alert(response.message);
		        	location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.flag){
					$scope.reloadList();//刷新列表
					$scope.selectIds = [];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
	// $scope.entity={goods:{},goodsDesc:{},itemList:[]}
	
	$scope.uploadFile = function(){
		// 调用uploadService的方法完成文件的上传
		uploadService.uploadFile().success(function(response){
			if(response.flag){
				// 获得url
				$scope.image_entity.url =  response.message;
			}else{
				alert(response.message);
			}
		});
	}
	
	// 获得了image_entity的实体的数据{"color":"褐色","url":"http://192.168.209.132/group1/M00/00/00/wKjRhFn1bH2AZAatAACXQA462ec665.jpg"}
	$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};
	
	$scope.add_image_entity = function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	
	$scope.remove_iamge_entity = function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	
	// 查询一级分类列表:
    /**
	 * 问题 1:如何查询一级分类?
	 * 答:去数据库查看 tb_item_cat 自关联分类表 :
	 * 要点:一级分类 是顶级分类 没有父  固定是0 :所以这里传递  0 去查询一级分类
	 * 		查询出来的一级分类结果:是一个集合:一级分类数据的集合:
	 * 		其中的每一个一级分类对象 都带有自己的二级分类的id;
	 * 		每一个二级分类的id:又包含着三级分类id集合:
	 * 		每一个三级分类还有id,但对应的是 具体的/详细的商品详情信息:tb_type_template模板表
	 * 		模板表对应着 各个级对应的具体选项信息
	 * 	析:利用上面的 循环关系:可以在页面端 反复的获取 分级id,
	 * 		反复的查询tb_item_cat 分级表,就能达到查询每一级 都有哪些信息,返回不同级的 级别集合
	 * 	页面 就能遍历出来每一个 级别集合,并遍历出来 处理,显示
	 *
	 * 问题 2:如何知道/查询二级分类?
	 * 答:一级本类下面 有好多信息:都是二级信息->从属于一级分类
	 *
	 * 问题 3:每一个三级分类还有id,但对应的是 具体的/详细的商品详情信息:tb_type_template模板表
	 * 		模板表中内部有brand_ids_表内容  spec_ids_表内容
	 * 		spec_ids_中有_spec_id ->	spec_option_表_有具体的该spec_id对应的更详细分类
     */
	$scope.selectItemCat1List = function(){
		//查询的是tb_item_cat表 3级分类信息 自关联表
		itemCatService.findByParentId(0).success(function(response){
			//返回一级分类信息集合
			$scope.itemCat1List = response;
		});
	}
	
	// 查询二级分类列表:当一级分类信息id变化后,被监听到后,监听器会拿着变化后的一级分类id,去查询该id对应的二级分类信息
	$scope.$watch("entity.goods.category1Id",function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat2List = response;
		});
	});
	
	// 查询三级分类列表:当二级分类变化后,监听器会拿着变化后的二级分类id,去查询该id对应的三级分类信息
	$scope.$watch("entity.goods.category2Id",function(newValue,oldValue){
		itemCatService.findByParentId(newValue).success(function(response){
			$scope.itemCat3List = response;
		});
	});

	// 查询模块ID:当三级分类变化后,监听器会拿着变化后的三级分类id,去查询该id对应的tb_type_template表中的对象集
	$scope.$watch("entity.goods.category3Id",function(newValue,oldValue){
		itemCatService.findOne(newValue).success(function(response){
			$scope.entity.goods.typeTemplateId = response.typeId;
		});
	});
//最后更新:这个id是tb_item_cat表id,tb_item_cat表中有tb_type_template表id,tb_item_cat就相当是中间表,

//	查询模板下的品牌列表:
//tb_type_template:表
//id    name   		brand_ids
//35	手机			[{"id":1,"text":"联想"},{"id":3,"text":"三星"},{"id":2,"text":"华为"},{"id":5,"text":"OPPO"},{"id":4,"text":"小米"},{"id":9,"text":"苹果"},{"id":8,"text":"魅族"},{"id":6,"text":"360"},{"id":10,"text":"VIVO"},{"id":11,"text":"诺基亚"},{"id":12,"text":"锤子"}]
//37	电视			[{"id":16,"text":"TCL"},{"id":13,"text":"长虹"},{"id":14,"text":"海尔"},{"id":19,"text":"创维"},{"id":21,"text":"康佳"},{"id":18,"text":"夏普"},{"id":17,"text":"海信"},{"id":20,"text":"东芝"},{"id":15,"text":"飞利浦"},{"id":22,"text":"LG"}]
	//当type_template id 变化后_页面会把每个对象遍历成一个一个小选项标签_这个标签都可以改变_代表id改变 ,
	//监听器会拿着type_template_id去查询 去查询下一个 tb_type_template对对象,拿到/遍历里面的brand_ids.text:该品牌下有哪些商家:联想 三星 华为 OPPO
	$scope.$watch("entity.goods.typeTemplateId",function(newValue,oldValue){
		// 根据模板ID查询模板的数据
		typeTemplateService.findOne(newValue).success(function(response){
			$scope.typeTemplate = response;//获取到模板中的下一条对象
			//获取到模板对象之后,直接从模板对象中取出brandIds 简化Json串
			// 将品牌的字符串数据转成JSON:将brandIds json串取出来 转换后,又放置回去
			$scope.typeTemplate.brandIds = JSON.parse( $scope.typeTemplate.brandIds );//获取对象中品牌信息集合
			
			// 将扩展属性的字符串转成JSON:扩展属性,也是从tb_typeTemplate表中取的customAttributeItems 简略对象,
			if($location.search()['id'] == null){
				//将扩展信息json串拿出来,解析成对象在 放回去
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse( $scope.typeTemplate.customAttributeItems );
			}
			
		});
		
		// 根据模板ID获得规格的列表的数据：也是在监测函数内:
		//		当模板对象type_template对象id变化时 其内部spec_ids简化对象也变了
		//		简化对象中的spec_id对应着 specifition_option表中的spec_id 即对应着规格分类表中的一个对象
		//		因此:通过type_template->specifition->specifition_option->
		//       type_template表中内含specifition表简化信息,简化信息中有自己的spec_id表示自己是specifition表中的哪一条信息
		//这个spec_id 又被Specifition_option表引用:对应相应的规格分类选项
		typeTemplateService.findBySpecList(newValue).success(function(response){
			//这个规格分类选项 最后被遍历到页面进行显示
			$scope.specList = response;
		});
	});
	
	$scope.updateSpecAttribute = function($event,name,value){
		// 调用封装的方法判断 勾选的名称是否存在:
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
	
		if(object != null){
			// 找到了
			if($event.target.checked){
				object.attributeValue.push(value);
			}else{
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
			}
			
			if(object.attributeValue.length == 0){
				var idx = $scope.entity.goodsDesc.specificationItems.indexOf(object);
				$scope.entity.goodsDesc.specificationItems.splice(idx,1);
			}
		}else{
			// 没找到
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	}
	
	// 创建SKU的信息:
	$scope.createItemList = function(){
		// 初始化基础数据:
		$scope.entity.itemList = [{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		
		var items = $scope.entity.goodsDesc.specificationItems;
		
		for(var i=0;i<items.length;i++){
			// 
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}
	
	addColumn = function(list,columnName,columnValues){
		// 定义一个集合用于保存生成的每行的数据:
		var newList = [];
		// 遍历该集合的数据:
		for(var i=0;i<list.length;i++){
			var oldRow = list[i];
			for(var j=0;j<columnValues.length;j++){
				// 对oldRow数据进行克隆:
				var newRow = JSON.parse( JSON.stringify(oldRow) );
				newRow.spec[columnName]=columnValues[j];
				// 将newRow存入到newList中
				newList.push(newRow);
			}
			
		}
		
		return newList;
	}
	
	// 显示状态
	$scope.status = ["未审核","审核通过","审核未通过","关闭"];

	//声明一个123级分类的数据:用来接收所有的分类
	$scope.itemCatList = [];
	// 显示分类:
	$scope.findItemCatList = function(){
		itemCatService.findAll().success(function(response){
			for(var i=0;i<response.length;i++){
				//将表结构的 分类,转换成数组 接口的分类
				$scope.itemCatList[response[i].id] = response[i].name;
			}
		});
	}
});	
