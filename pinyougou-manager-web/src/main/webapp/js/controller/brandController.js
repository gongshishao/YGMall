//品牌控制层
app.controller('brandController' ,function($scope,brandService){
    //读取列表数据绑定到表单中
    $scope.findAll=function(){
        brandService.findAll().success(
            function(response){
                $scope.list=response;
            }
        );
    }

    //分页
    $scope.findByPage = function (pageNum, pageSize) {
        brandService.findByPage(pageNum, pageSize).success(
            function (response) {
                $scope.list = response.rows;//获得当前页数据,对应PageResult中的rows
                $scope.paginationConf.totalItems = response.total;//更新总记录数,对应PageResult中的total
            }
        );
    }

});
