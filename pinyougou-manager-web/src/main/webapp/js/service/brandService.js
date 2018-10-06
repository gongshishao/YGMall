//品牌服务层
app.service('brandService',function($http){
    //读取列表数据绑定到表单中
    this.findAll=function(){
        return $http.get('../brand/findAll.do');
    }

    this.findByPage = function (pageNum, pageSize) {
       return $http.get("../brand/findByPage.do?pageNum=" + pageNum + "&pageSize=" + pageSize).success(

        );
    }
});
