package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 测试时用来手动向solr批量插入索引，批量删除索引，
 * 在商品通过审核和删除的方法里添加solr同步索引后就自动生成索引了
 */
@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 导入商品数据
     */
    public void importItemData(){
        TbItem where = new TbItem();
        //只导入已审核的商品
        where.setStatus("1");
        List<TbItem> items = itemMapper.select(where);
        //solr对象列表
        List<SolrItem> solrItemList = new ArrayList<>();
        SolrItem solrItem = null;

        System.out.println("-------------商品列表开始-------------");
        for (TbItem item : items) {
            System.out.println(item.getId() + " " + item.getTitle() + "  " + item.getPrice());

            solrItem = new SolrItem();
            //使用spring的BeanUtils深克隆对象
            BeanUtils.copyProperties(item,solrItem);

            //将spec字段中的json字符串转换为map
            Map specMap = JSON.parseObject(item.getSpec());
            solrItem.setSpecMap(specMap);

            solrItemList.add(solrItem);
        }
        System.out.println("-------------商品列表结束-------------");
        System.out.println("总计插入数据："+solrItemList.size()+"条");
        //保存数据到solr
        solrTemplate.saveBeans(solrItemList);
        solrTemplate.commit();
    }

    public void deleteAll() {
        Query query = new SimpleQuery("*:*");
        //删除所有数据
        solrTemplate.delete(query);
        solrTemplate.commit();
        System.out.println("批量删除成功");
    }


    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        solrUtil.importItemData();//批量插入
        //solrUtil.deleteAll();//批量删除

    }


}

