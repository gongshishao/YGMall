package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import entity.SolrItem;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * 搜索服务消息监听器
 *  同步
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018/10/24
 */
@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            TextMessage msg = (TextMessage) message;
            String json = msg.getText();
            //把json转成itemList
            List<TbItem> itemList = JSON.parseArray(json, TbItem.class);
            //solr对象列表
            List<SolrItem> solrItemList = new ArrayList<>();
            SolrItem solrItem = null;
            for (TbItem item : itemList) {
                solrItem = new SolrItem();
                //使用spring的BeanUtils深克隆对象
                BeanUtils.copyProperties(item, solrItem);
                //将spec字段中的json字符串转换为map
                Map specMap = JSON.parseObject(item.getSpec());
                solrItem.setSpecMap(specMap);
                solrItemList.add(solrItem);
            }
            itemSearchService.importList(solrItemList);
            System.out.println("成功导入索引库");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
