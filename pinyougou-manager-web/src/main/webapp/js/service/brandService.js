//品牌服务层
app.service('brandService',function($http){
    //读取列表数据绑定到表单中
    this.findAll=function(){
        return $http.get('../brand/findAll.do');
    }

    //分页查询方法
    this.findByPage = function (pageNum, pageSize) {
       return $http.get("../brand/findByPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize);
    }

    //根据id查询品牌信息
    this.findOne=function (id) {
        return $http.get("../brand/findOne.do?id="+id);
    }

    //增加
    this.addBrand=function(entity){
        return  $http.post('../brand/addBrand.do',entity );
    }
    //修改
    this.update=function(entity){
        return  $http.post('../brand/update.do',entity );
    }

    //批量删除操作
    this.dele=function (selectIds) {
        return $http.get("../brand/delete.do?ids="+selectIds);
    }

    //条件查询
    this.search=function (pageNum, pageSize,searchEntity) {
        return $http.post("../brand/search.do?pageNum=" + pageNum + "&pageSize=" + pageSize ,searchEntity);
    }

});
