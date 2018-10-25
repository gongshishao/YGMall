package com.pinyougou.page.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.File;

/**
 * 静态页面消息监听器，删除静态化页面
 */
@Component
public class PageDeleteListener implements MessageListener {

    @Override
    public void onMessage(Message message) {
        try {
            //获取消息内容
            ObjectMessage msg = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) msg.getObject();
            //遍历生成html
            for (Long goodsId : goodsIds) {
                boolean result = deletFile(goodsId);
                System.out.println("删除商品 "+ goodsId + " 静态页：" + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Value("${pagedir}")
    private String pagedir;
    /**
     * 删除商品详情页
     * @param goodsId
     * @return 删除结果
     */
    private boolean deletFile(Long goodsId){
        return new File(pagedir + goodsId + ".html").delete();
    }
}
