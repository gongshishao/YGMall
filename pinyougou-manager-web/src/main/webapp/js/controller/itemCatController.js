 //控制层 ,额外引入typeTemplateService模板类型,提供给select2下拉框组件使用
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
            $scope.entity.parentId=$scope.parentId;//记住上级ID
			serviceObject=itemCatService.add( $scope.entity );//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
                    alert(response.message);
					//重新查询 
		        	//$scope.reloadList();//重新加载
                    $scope.findByParentId($scope.parentId);//重新加载

                }else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		itemCatService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
                    alert(response.message);
                    $scope.findByParentId($scope.parentId);//重新加载
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	//初始化变量
	$scope.parentId=0;
    //根据父类id搜索类目列表
	$scope.findByParentId=function (parentId) {
		$scope.parentId=parentId;//记住上级ID
		itemCatService.findByParentId(parentId).success(
			function (response) {
                $scope.list = response;
            }
		);
    }

    //面包屑导航,默认页面加载时在第一级目录
	$scope.grade=1;
	//设置类目分级
	$scope.setGrade=function (value) {
		$scope.grade=value;
    }

    //给面包屑添加记录
    $scope.selectList=function (v_entity) {
        if ($scope.grade == 1) {
            $scope.entity_1 = null;
            $scope.entity_2 = null;
        }else if ($scope.grade == 2) {
            $scope.entity_1 = v_entity;
            $scope.entity_2 = null;
        } else {
            $scope.entity_2 = v_entity;
        }

        //最后调用一下查询当前目录列表
        this.findByParentId(v_entity.id);
    }

    //类型模板列表
    $scope.typeTemplateList={data:[]};
    $scope.findTypeTemplate=function () {
        typeTemplateService.findAll().success(
            function (response) {
                //删除多余的属性
                for (var k = 0; k < response.length; k++) {
                    delete response[k]["brandIds"];
                    delete response[k]["customAttributeItems"];
                    delete response[k]["specIds"];
                    delete response[k]["name"];
                }
                $scope.typeTemplateList={data:response};
            }
        );
    }

});	
