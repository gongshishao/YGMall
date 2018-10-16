package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * 商品和商品描述的组合类
 *
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.pojogroup
 * @date 2018/10/12
 */

public class Goods implements Serializable {
    //商品SPU
    private TbGoods goods;

    //商品描述
    private TbGoodsDesc goodsDesc;

    //商品SKU列表
    private List<TbItem> itemList;

    public TbGoods getGoods() {
        return goods;
    }
    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }
    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }
    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }
    public List<TbItem> getItemList() {
        return itemList;
    }
    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }

}
