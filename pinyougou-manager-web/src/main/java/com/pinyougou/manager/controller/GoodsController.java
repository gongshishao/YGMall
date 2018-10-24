package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.search.service.ItemSearchService;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import entity.SolrItem;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 请求处理器
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

    @Reference
    private ItemSearchService itemSearchService;

    @Reference(timeout=100000)
    private ItemPageService itemPageService;

    /**
     * 生成静态页（测试）
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public boolean genHtml(Long goodsId) {
        return itemPageService.genItemHtml(goodsId);
    }


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
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            itemSearchService.deleteByGoodsIds(ids);
            logger.info("删除商品同步删除索引");
            return new Result(true, "删除成功");
        } catch (Exception e) {
            logger.error("商品删除发生错误,原因是:"+e);
            return new Result(false, "删除失败");
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

    /**
     * 运营商审核商品,改变商品状态
     *  1.商品审核过后把商品存入solr库
     *  2.审核通过后还需要立即根据新增商品时填写的数据，生成静态页面到指定目录下
     * @param ids --批量勾选的商品id
     * @param status --审核后的状态码
     * @return
     */
    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids,String status) {
        try {
            goodsService.updateStatus(ids,status);
            if ("1".equals(status)) {//如果审核通过
                //查询此次审核通过的商品
                List<TbItem> items = goodsService.findItemListByGoodsIdsAndStatus(ids, status);
                //由于更新插入到索引库用的是list<SolrItem>，所以需要将查询到的TbItem数据封装到SolrItem中
                if (items != null && items.size() > 0) {
                    ArrayList<SolrItem> solrItemsList = new ArrayList<>();
                    SolrItem solrItem = null;
                    for (TbItem item : items) {
                        solrItem = new SolrItem();
                        //使用spring的BeanUtils深克隆对象
                        BeanUtils.copyProperties(item, solrItem);
                        //将spec字段中的json字符串转换为map
                        Map specMap = JSON.parseObject(item.getSpec());
                        solrItem.setSpecMap(specMap);
                        solrItemsList.add(solrItem);
                    }
                    //2.生成静态页面
                    for (Long goodsId : ids) {
                        itemPageService.genItemHtml(goodsId);
                    }
                    //1.同步索引库
                    itemSearchService.importList(solrItemsList);

                    logger.info("商品审核成功同步索引库+静态化商品详情页");
                } else {
                    logger.info("没有找到SKU数据");
                }
            }
            return new Result(true, "操作成功");
        } catch (Exception e) {
            logger.error("商品审核发生错误,原因是:"+e);
            return new Result(false, "操作失败");
        }
    }


}
