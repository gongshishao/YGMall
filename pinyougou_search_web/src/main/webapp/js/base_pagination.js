var app=angular.module('pinyougou',['pagination']);//定义带分页的模板
//html信任过滤器,solr搜索无法高亮显示
app.filter("trustHtml",['$sce',function ($sce) {
    //data为被转换的内容
    return function (data) {
        //返回转换后的内容
        return $sce.trustAsHtml(data);
    }
}]);
