package com.pinyougou.search.service.impl;

import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
/**
 * 搜索服务消息监听器
 *  删除
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.search.service.impl
 * @date 2018/10/24
 */
@Component
public class ItemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            ObjectMessage msg = (ObjectMessage) message;
            Long[] ids = (Long[]) msg.getObject();
            //删除索引
            itemSearchService.deleteByGoodsIds(ids);
            System.out.println("成功删除索引库");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
