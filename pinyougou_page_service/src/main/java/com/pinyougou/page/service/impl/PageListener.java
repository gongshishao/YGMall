package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * 静态页面消息监听器，生成静态化页面
 */
@Component
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            ObjectMessage msg = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) msg.getObject();
            //遍历生成html
            for (Long goodsId : goodsIds) {
                boolean result = itemPageService.genItemHtml(goodsId);
                System.out.println("生成商品 "+ goodsId + " 静态页：" + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
