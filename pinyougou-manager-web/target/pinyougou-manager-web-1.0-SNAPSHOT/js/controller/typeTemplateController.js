 //控制层 
app.controller('typeTemplateController' ,function($scope,$controller   ,typeTemplateService,brandService,specificationService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		typeTemplateService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		typeTemplateService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		typeTemplateService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//此处不同于规格查询,返回的数据为json数组,需要解析
				//转换品牌列表为json
                $scope.entity.brandIds=JSON.parse($scope.entity.brandIds);
                //转换品牌列表为json
                $scope.entity.specIds=JSON.parse($scope.entity.specIds);
                //转换品牌列表为json
                $scope.entity.customAttributeItems = JSON.parse($scope.entity.customAttributeItems);
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=typeTemplateService.update( $scope.entity ); //修改  
		}else{
			serviceObject=typeTemplateService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
                    alert(response.message);
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
		typeTemplateService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
                    alert(response.message);
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		typeTemplateService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

    //品牌列表
    $scope.brandList={data:[]};
	$scope.findBrandList=function () {
        brandService.findAll().success(
        	function (response) {
                //删除多余的属性
                for (var k = 0; k < response.length; k++) {
                    delete response[k]["firstChar"];
                    delete response[k]["name"];
                }

                $scope.brandList={data:response};
            }
		);
    }

    //规格列表
    $scope.specList={data:[]};
	$scope.findSpecList=function () {
        specificationService.findAll().success(
        	function (response) {
                //删除多余的属性
                for (var k = 0; k < response.length; k++) {
                    delete response[k]["specName"];
                }

                $scope.specList={data:response};
            }
		);
    }

    //根据需求输出json串
	//jsonString要转换的json串,key要读取的值
    $scope.jsonToString=function (jsonString,key) {
        var json = JSON.parse(jsonString);
        var result = "";
        for(var i = 0;i < json.length; i++){
            if(i > 0){
                result += ",";
            }
            result += json[i][key];
        }
        return result;
    }

    //新增扩展属性行按钮的点击事件
    $scope.addTableRow=function(){
        $scope.entity.customAttributeItems.push({});
    }

    //删除扩展属性行
    $scope.deleTableRow=function (index) {
        $scope.entity.customAttributeItems.splice(index, 1);//根据索引值从集合中删除选项行
    }



});	
