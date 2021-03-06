package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 请求处理器
 *
 * @author Steven
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    private Logger logger = Logger.getLogger(GoodsController.class);
    @Reference
    private GoodsService goodsService;

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
     * 新增商品,注意要设置商品的selleId,从认证的登录用户获取
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        goods.getGoods().setSellerId(name);//商家用户名即商品里的sellerId
        try {
            goodsService.add(goods);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            logger.error("新增发布商品失败,原因是:" + e);
            return new Result(false, "增加失败");
        }
    }

    /**
     * 修改,修改之前先判断是否是具有商家权限
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        //验证修改权限，商家只能修改自己的商品
        Goods beUpdate = goodsService.findOne(goods.getGoods().getId());
        //已登录的商家
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ( !sellerId.equals(goods.getGoods().getSellerId()) || !sellerId.equals(beUpdate.getGoods().getSellerId()) ) {
            //如果当前修改的商品不是当前登录商家的,提示操作非法
            new Result(false, "当前操作非法");
        }
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            logger.error("修改商品失败,原因是:" + e);
            return new Result(false, "修改失败");
        }
    }

    /**
     * 根据商品id查询商品包装类SPU
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
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
        String sellerId = SecurityContextHolder.getContext().getAuthentication().getName();
        //添加查询条件
        goods.setSellerId(sellerId);
        return goodsService.findPage(goods, page, rows);
    }

}
