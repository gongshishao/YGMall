//控制层
app.controller('goodsController', function ($scope, $controller, itemCatService, goodsService, typeTemplateService, uploadService) {

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

    //查询实体
    $scope.findOne = function (id) {
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    //保存
    $scope.save = function () {
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert(response.message);
                    //重新查询
                    //清空实体
                    $scope.entity = {};
                    editor.html('');//清空富文本编辑器
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

    //文件上传
    $scope.upload = function () {
        uploadService.uploadFile().success(
            function (response) {
                //上传成功则绑定到表单
                if (response.success) {
                    $scope.img_entity.url = response.message;
                } else {
                    alert(response.message);
                }
            }).error(function () {
            alert("上传发生错误");
        });
    }

    //定义商品实体结构,注意itemImages图片项是包括颜色和图片路径的数组字段,并且可以有多个组合,对一个商品进行多项描述
    $scope.entity = {goods: {}, goodsDesc: {itemImages: []}};
    //保存图片,此处只是上传到页面图片列表,并未保存到数据库
    $scope.add_image_entity = function () {
        //添加图片列表
        $scope.entity.goodsDesc.itemImages.push($scope.img_entity);
    }

    //移除remove_image_entity
    $scope.remove_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index, 1);
    }

    //*****************************实现商品选择(三级分类)********************
    //查询一级分类
    $scope.selectItemCat1List = function () {
        itemCatService.findByParentId(0).success(function (response) {
            $scope.itemCat1List = response;
        })
    }

    //根据一级类目，更新二级类目
    //$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数
    $scope.$watch("entity.goods.category1Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat2List = response;
        })
    })

    //根据二级类目，更新三级类目
    $scope.$watch("entity.goods.category2Id", function (newValue, oldValue) {
        itemCatService.findByParentId(newValue).success(function (response) {
            $scope.itemCat3List = response;
        })
    })

    //选择三级类目后，更新模板id
    //$watch方法用于监控某个变量的值，当被监控的值发生变化，就自动执行相应的函数
    $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
        itemCatService.findOne(newValue).success(function (response) {
            $scope.entity.goods.typeTemplateId = response.typeId;
        })
    });

    //根据模板id，更新品牌列表,规格列表
    $scope.$watch("entity.goods.typeTemplateId", function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(function (response) {
            $scope.typeTemplate = response;
            $scope.typeTemplate.brandIds = JSON.parse(response.brandIds);

            //在用户更新模板ID时，读取模板中的扩展属性赋给商品的扩展属性。
            $scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
            //获得规格列表
            typeTemplateService.findSpecList(newValue).success(function (response) {
                $scope.specList = response;
            })
        })
    });

    /**
     * 勾选页面上的规格时调用此函数
     * @param $event 当前点击的checkbox
     * @param name 规格的名称
     * @param value 规格选项的值
     */
    $scope.updateSpecAttribute = function ($event, name, value) {
        //查找规格有没有保存过
        var obj = this.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        //找到相关记录
        if (obj != null) {
            //如果已选中
            if ($event.target.checked) {
                obj.attributeValue.push(value);
            } else { //取消勾选
                //查找当前value的下标
                var idx = $scope.selectIds.indexOf(value);
                //删除数据
                obj.attributeValue.splice(idx, 1);

                //取消勾选后，如果当前列表里没有记录时，删除当前整个规格
                if (obj.attributeValue.length == 0) {
                    var valueIndex = $scope.entity.goodsDesc.specificationItems.indexOf(obj);
                    $scope.entity.goodsDesc.specificationItems.splice(valueIndex, 1);
                }
            }
        } else {
            //添加一条记录
            $scope.entity.goodsDesc.specificationItems.push(
                {'attributeName': name, 'attributeValue': [value]});
        }
    }


});
