//品牌控制层,最通用的控制器继承
app.controller('baseController', function ($scope) {

    //重新加载列表 数据
    $scope.reloadList = function () {
        //切换页码
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.reloadList();//重新加载
        }
    };

    $scope.selectIds = [];//选中的ID集合

    /**
     * 更新复选框
     * @param $event
     * @param id
     */
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {//如果是被选中,则增加到数组
            $scope.selectIds.push(id);
        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除 
        }
    }

    /**
     *  遍历List<Map>，查找对应的数据
     * @param list 搜索的列表
     * @param key   搜索的key
     * @param keyValue 对比的值
     * @returns {*}
     */
    $scope.searchObjectByKey = function (list, key, keyValue) {
        for (var i = 0; i < list.length; i++) {
            //如果找到相应key，返回找到的对象
            if (list[i][key] == keyValue) {
                return list[i];
            }
        }
        //找不到返回空
        return null;
    }


});