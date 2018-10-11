//品牌控制层
app.controller('brandController' ,function($scope,$controller,brandService){
    $controller('baseController',{$scope:$scope});//继承baseController.js,伪继承

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

    //查询品牌实体
    $scope.findOne=function (id) {
       brandService.findOne(id).success(
            function (response) {
                $scope.entity=response;
            }
        );
    }

    //增加品牌数据,*****注：因为与修改是同一编辑窗口，所以先进行方法判断，根据点击打开时是否附带id
    //保存和修改公用一个方法，所以service层的调用之前先进行方法的判断
    $scope.save = function () {
        var serviceMethod;//声明一个service层的的调用
        if ($scope.entity.id != null) {
            serviceMethod = brandService.update($scope.entity);
        } else {
            serviceMethod = brandService.addBrand($scope.entity);
        }
        serviceMethod.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //获得需要批量删除的ids数组
    $scope.selectIds=[];
    //批量删除操作
    $scope.dele=function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    $scope.reloadList();//重新加载
                    $scope.selectIds=[];
                } else {
                    alert(response.message);
                }
            })
    }

    //条件查询
    $scope.searchEntity={};
    $scope.search=function (pageNum, pageSize) {
        brandService.search(pageNum, pageSize,$scope.searchEntity).success(
                function (response) {
                $scope.list = response.rows;//获得当前页数据,对应PageResult中的rows
                $scope.paginationConf.totalItems = response.total;//更新总记录数,对应PageResult中的total
            }
        );
    }



});
