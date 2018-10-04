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
    //其它方法省略........
});
