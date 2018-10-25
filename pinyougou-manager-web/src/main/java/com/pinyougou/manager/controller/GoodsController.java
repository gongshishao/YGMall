package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.List;

/**
 * 请求处理器
 * 商品管理控制层，商品审核通过后同步索引库及生成静态化页面，使用activemq解耦
 *
 * @author Steven
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    //引入log
    private Logger logger = Logger.getLogger(GoodsController.class);

    @Reference
    private GoodsService goodsService;

    //同步索引库，已通过activemq解耦
    //@Reference
    //private ItemSearchService itemSearchService;
    //注入消息模板和队列
    @Autowired
    private Destination queueSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;

    @Autowired
    private Destination topicPageDestination;
    @Autowired
    private Destination topicPageDeleteDestination;

    @Autowired
    private JmsTemplate jmsTemplate;

    //@Reference(timeout=100000)
    //private ItemPageService itemPageService;

    /**
     * 生成静态页（测试）
     * @param goodsId
     */
   /* @RequestMapping("/genHtml")
    public boolean genHtml(Long goodsId) {
        return itemPageService.genItemHtml(goodsId);
    }*/

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除，删除商品时把solr索引库对应的索引也删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            goodsService.delete(ids);
            //从索引库中删除
            //itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage(ids);
                }
            });
            //删除商品详情页
            jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    //发送序列化对象消息
                    return session.createObjectMessage(ids);
                }
            });


            logger.info("删除商品同步删除索引+删除静态页面");
            return new Result(true, "删除成功");
        } catch (Exception e) {
            logger.error("商品删除发生错误,原因是:" + e);
            return new Result(false, "删除失败");
        }
    }

    /**
     * 运营商审核商品,改变商品状态
     * 1.商品审核过后把商品存入solr库
     * 2.审核通过后还需要立即根据新增商品时填写的数据，生成静态页面到指定目录下
     *
     * @param ids    --批量勾选的商品id
     * @param status --审核后的状态码
     * @return
     */
    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids, String status) {
        try {
            goodsService.updateStatus(ids, status);
            if ("1".equals(status)) {//如果审核通过
                //查询此次审核通过的商品，传递SKU
                List<TbItem> items = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
                //1.同步索引库，已通过activemq解耦
                /*itemSearchService.importList(solrItemsList);*/
                //把sku数据作为消息发送到队列
                final String jsonString = JSON.toJSONString(items);
                jmsTemplate.send(queueSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(jsonString);
                    }
                });
                //2.生成静态页面，传递SPU的ids
               /* for (Long goodsId : ids) {
                    itemPageService.genItemHtml(goodsId);
                }*/
                final Long[] goodsIds = ids;
                jmsTemplate.send(topicPageDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createObjectMessage(goodsIds);
                    }
                });

                logger.info("通过activemq消息队列实现，商品审核成功后同步索引库+静态化商品详情页");
            }
            return new Result(true, "操作成功");
        } catch (Exception e) {
            logger.error("商品审核发生错误,原因是:" + e);
            return new Result(false, "操作失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param goods
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

}
