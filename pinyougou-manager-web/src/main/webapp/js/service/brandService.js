//品牌服务层
app.service('brandService',function($http){
    //读取列表数据绑定到表单中
    this.findAll=function(){
        return $http.get('../brand/findAll.do');
    }

    this.findByPage = function (pageNum, pageSize) {
        $http.get("../brand/findByPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize).success(
            function (response) {
                $scope.list = response.rows;//获得当前页数据,对应PageResult中的rows
                $scope.paginationConf.totalItems = response.total;//更新总记录数,对应PageResult中的total
            }
        );
    }
});
