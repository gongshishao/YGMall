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
});
