package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台搜索模块
 *
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018/10/22
 */
@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 搜索核心方法，需要实现以下优化
     * （1）搜索面板的商品分类需要使用Spring Data Solr的分组查询来实现
     * （2）为了能够提高查询速度，我们需要把查询面板的品牌、规格数据提前放入redis
     * 将商品分类数据、品牌数据、和规格数据都放入Redis存储
     *
     * @param searchMap 查询条件列表
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        Map<String, Object> map = new HashMap<>();
        //1.按关键字查询（高亮显示）
        //关键词空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords", keywords.replace(" ", ""));
        //1.根据关键字查询商品
        map.putAll(searchList(searchMap));
        //2.根据关键字查询商品分类
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        //3.根据商品分类名称查询品牌与规格列表
        String categoryName = (String) searchMap.get("category");
        if (!"".equals(categoryName)) {//如果有分类名称
            map.putAll(searchBrandAndSpecList(categoryName));
        } else {//如果没有分类名称，按照第一个查询
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;

    }

    /**
     * 批量导入数据
     *
     * @param list
     */
    @Override
    public void importList(List list) {
        solrTemplate.saveBean(list);
        solrTemplate.commit();
    }

    /**
     * 根据商品id删除索引
     *
     * @param goodsIdList
     */
    @Override
    public void deleteByGoodsIds(Long[] goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 跟据商品分类名称查询商品品牌与规格列表
     *
     * @param category 商品分类名称
     * @return
     */
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //跟据商品分类名称查询模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //跟据模板ID查询品牌列表
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);
            //跟据模板ID查询规格列表
            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);
        }
        return map;
    }

    /**
     * 查询分类列表-使用spring data solr的分组查询
     *
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();
        //1.   创建查询条件对象query = new SimpleQuery()
        Query query = new SimpleQuery();
        //2.   复制之前的Criteria组装查询条件的代码
        //组装查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //3.   创建分组选项对象new GroupOptions().addGroupByField(域名)
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        //4.   设置分组对象query.setGroupOptions
        query.setGroupOptions(groupOptions);
        //5.   得到分组页对象page = solrTemplate.queryForGroupPage
        GroupPage<SolrItem> page = solrTemplate.queryForGroupPage(query, SolrItem.class);
        //6.   得到分组结果集groupResult = page.getGroupResult(域名)
        GroupResult<SolrItem> groupResult = page.getGroupResult("item_category");
        //7.   得到分组结果入口groupEntries = groupResult.getGroupEntries()
        Page<GroupEntry<SolrItem>> groupEntries = groupResult.getGroupEntries();
        //8.   得到分组入口集合content = groupEntries.getContent()
        List<GroupEntry<SolrItem>> content = groupEntries.getContent();
        //9.   遍历分组入口集合content.for(entry)，记录结果entry.getGroupValue()
        for (GroupEntry<SolrItem> entry : content) {
            list.add(entry.getGroupValue());
        }
        return list;
    }

    /**
     * 抽取跟据关键字搜索数据列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        String keywords = (String) searchMap.get("keywords");
        if (StringUtils.isNotBlank(keywords)) {
            //1.构建query高亮查询对象new SimpleHighlightQuery
            HighlightQuery query = new SimpleHighlightQuery();

            //2.高亮选项初始化：调用query.setHighlightOptions()方法，构建高亮数据三步曲：new HighlightOptions().addField(高亮业务域)，.setSimpleP..(前缀)，.setSimpleP..(后缀)
            HighlightOptions options = new HighlightOptions().addField("item_title");
            options.setSimplePrefix("<em style='color:red;'>");
            options.setSimplePostfix("</em>");
            query.setHighlightOptions(options);

            //3.组装查询条件，搜索框输入关键字查询
            Criteria criteria = new Criteria("item_keywords").is(keywords);
            query.addCriteria(criteria);

            //4.添加其他查询条件
            //4.1 按商品分类过滤
            if (!"".equals(searchMap.get("category"))) {//如果用户选择了分类
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            //4.2按品牌过滤
            if (!"".equals(searchMap.get("brand"))) {//如果用户选择了品牌
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            //4.3 按规格过滤
            if (searchMap.get("spec") != null) {
                Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
                for (String key : specMap.keySet()) {

                    FilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                    filterQuery.addCriteria(filterCriteria);
                    query.addFilterQuery(filterQuery);

                }

            }
            //4.3 按价格过滤
            if (!"".equals(searchMap.get("price"))) {
                String[] price = ((String) searchMap.get("price")).split("-");
                if (!price[0].equals("0")) {//如果区间起点不等于0
                    Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                    FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
                if (!price[1].equals("*")) {//如果区间终点不等于*
                    Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                    FilterQuery filterQuery = new SimpleFilterQuery(filterCriteria);
                    query.addFilterQuery(filterQuery);
                }
            }
            //4.4 分页查询
            Integer pageNo = (Integer) searchMap.get("pageNo");//提取页码
            if (pageNo == null) {
                pageNo = 1;//默认第一页
            }
            Integer pageSize = (Integer) searchMap.get("pageSize");//每页记录数
            if (pageSize == null) {
                pageSize = 20;//默认20
            }
            query.setOffset((pageNo - 1) * pageSize);//从第几条记录查询
            query.setRows(pageSize);

            //4.5排序
            String sortValue = (String) searchMap.get("sort");//ASC  DESC
            String sortField = (String) searchMap.get("sortField");//排序字段
            if (sortValue != null && !sortValue.equals("")) {
                if (sortValue.equals("ASC")) {
                    Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                    query.addSort(sort);
                }
                if (sortValue.equals("DESC")) {
                    Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                    query.addSort(sort);
                }
            }
            //5.   接收solrTemplate.queryForHighlightPage的返回数据，定义page变量
            HighlightPage<SolrItem> page = solrTemplate.queryForHighlightPage(query, SolrItem.class);
            //6.   遍历解析page对象，page.getHighlighted().for，item = h.getEntity()，
            //item.setTitle(h.getHighlights().get(0).getSnipplets().get(0))，在设置高亮之前最好判断一下;
            for (HighlightEntry<SolrItem> h : page.getHighlighted()) {
                SolrItem item = h.getEntity();
                //设置高亮数据,先判断是否有高亮数据
                if (h.getHighlights() != null && h.getHighlights().size() > 0 && h.getHighlights().get(0).getSnipplets() != null && h.getHighlights().get(0).getSnipplets().size() > 0) {
                    item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));
                }
            }
            //7.   在循环完成外map.put("rows", page.getContent())返回数据列表
            map.put("rows", page.getContent());
            map.put("totalPages", page.getTotalPages());//返回总页数
            map.put("total", page.getTotalElements());//返回总记录数
        }
        return map;
    }

}
