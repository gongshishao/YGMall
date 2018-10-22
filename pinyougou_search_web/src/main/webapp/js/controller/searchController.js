app.controller('searchController',function($scope,searchService){
    //搜索
    $scope.search=function(){
        searchService.search( $scope.searchMap ).success(
            function(response){
                //搜索返回的结果
                $scope.resultMap=response;
            }
        );
    }

    /**
     * 搜索对象
     * @type {{keywords: 关键字, category: 商品分类, brand: 品牌, spec: {'网络'：'移动4G','机身内存':'64G'}}}
     */
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{}};

    /**
     * 添加搜索项
     * @param key
     * @param value
     */
    $scope.addSearchItem=function(key,value){
        if(key=="category" ||  key=="brand"){//如果是分类或品牌
            $scope.searchMap[key]=value;
        }else{//否则是规格
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    }


    /**
     * 移除复合搜索条件
     * @param key
     */
    $scope.removeSearchItem=function(key){
        if(key=="category" ||  key=="brand"){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }



});
