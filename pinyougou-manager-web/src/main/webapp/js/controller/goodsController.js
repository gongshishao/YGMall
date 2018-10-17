//控制层
app.controller('goodsController', function ($scope, $controller, goodsService,itemCatService) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //查询实体,改写商品的查询方法
    $scope.findOne = function (id) {
        var id = $location.search()['id'];//获取参数值
        if (id == null) {
            return;
        }//如果有id才查询实体
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
                //回显富文本编辑器中的内容
                editor.html($scope.entity.goodsDesc.introduction);
                //回显图片列表
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //回显扩展属性(此处与$scope.$watch中根据模板id更新扩展属性的方法冲突,被覆盖了)
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //回显规格属性
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
                //回显SKU列表
                for (var i = 0; i <$scope.entity.itemList.length ; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }

            }
        )
    }

    //保存
    $scope.save = function () {
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    //重新查询
                    $scope.reloadList();//重新加载
                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.reloadList();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //添加商品状态数组
    $scope.status = ['未审核', '已审核', '审核未通过', '关闭'];//商品状态

    $scope.itemCatList = [];//商品分类列表
    //加载商品分类列表
    $scope.findItemCatList = function () {
        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {
                    $scope.itemCatList[response[i].id] = response[i].name;//将分类名赋予id
                }
            }
        );
    }

    /**
     * 更新商品状态
     */
    $scope.updateStatus=function () {
        goodsService.updateStatus($scope.selectIds,status).success(function (response) {
            alert(response.message);
            //如果修改成功
            if(response.success){
                $scope.reloadList();//刷新列表
                $scope.selectIds=[];//清空ID集合
            }
        })
    }
});	
