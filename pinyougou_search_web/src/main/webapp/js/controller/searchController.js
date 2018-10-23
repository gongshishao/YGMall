app.controller('searchController',function($scope,searchService){

    /**
     * 搜索对象
     * @type {{keywords: 关键字, category: 商品分类, brand: 品牌, spec: {'网络'：'移动4G','机身内存':'64G'}，price:价格}}
     */
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};
    //搜索
    $scope.search=function(){
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;//搜索结果中的页码需要转成int数据类型，否则跳转页码的功能按钮会失效
        searchService.search( $scope.searchMap ).success(
            function(response){
                //搜索返回的结果
                $scope.resultMap=response;
                buildPageLabel();//调用
            }
        );
    }


    //判断关键字是不是品牌
        $scope.keywordsIsBrand=function(){
            for(var i=0;i<$scope.resultMap.brandList.length;i++){
                if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){//如果包含
                    return true;
                }
            }
            return false;
        }

    //分页查询
    $scope.queryByPage=function(pageNo){
        if(pageNo<1 || pageNo>$scope.resultMap.totalPages){
            return ;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();//查询
    }

    /**
     * 添加搜索项
     * @param key
     * @param value
     */
    $scope.addSearchItem=function(key,value){
        if(key=="category" ||  key=="brand" || key=='price'){//如果是分类或品牌或价格
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
        if(key=="category" ||  key=="brand" || key=='price'){//如果是分类或品牌或价格
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    }

    //设置排序规则
    $scope.sortSearch=function(sortField,sort){
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    }


    //构建分页栏
    buildPageLabel=function(){
        //构建分页栏
        $scope.pageLabel=[];
        var firstPage=1;//开始页码
        var lastPage=$scope.resultMap.totalPages;//截止页码
        $scope.firstDot=true;//默认前面有点
        $scope.lastDot=true;//默认后边有点
        if($scope.resultMap.totalPages>5){  //如果页码数量大于5
            if($scope.searchMap.pageNo<=3){//并且当前页码小于等于3 ，显示前5页
                lastPage=5;
                $scope.firstDot=false;//并且前面没点
            }else if( $scope.searchMap.pageNo>= $scope.resultMap.totalPages-2 ){//显示后5页
                firstPage=$scope.resultMap.totalPages-4;
                $scope.lastDot=false;//后边没点
            }else{  //显示以当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }
        //构建页码
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    }

    //判断当前页为第一页
    $scope.isTopPage=function(){
        if($scope.searchMap.pageNo==1){
            return true;
        }else{
            return false;
        }
    }

    //判断当前页是否未最后一页
    $scope.isEndPage=function(){
        if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }



});
