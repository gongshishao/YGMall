 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,goodsService,uploadService){
	
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
	
	//查询实体 
	$scope.findOne=function(id){				
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}

	//保存 
	$scope.save=function(){
        $scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    alert(response.message);
					//重新查询
                    //清空实体
                    $scope.entity={};
                    editor.html('');//清空富文本编辑器
                    $scope.reloadList();//重新加载
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
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
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

	//文件上传
	$scope.upload=function () {
        uploadService.uploadFile().success(
            function (response) {
                //上传成功则绑定到表单
                if (response.success) {
                    $scope.img_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生错误");
        });
    }

    //定义商品实体结构,注意itemImages图片项是包括颜色和图片路径的数组字段,并且可以有多个组合,对一个商品进行多项描述
    $scope.entity = {goods: {}, goodsDesc: {itemImages: []}};
    //保存图片,此处只是上传到页面图片列表,并未保存到数据库
	$scope.add_image_entity=function () {
        //添加图片列表
        $scope.entity.goodsDesc.itemImages.push($scope.img_entity);
    }
    
    //移除remove_image_entity
	$scope.remove_image_entity=function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

});
