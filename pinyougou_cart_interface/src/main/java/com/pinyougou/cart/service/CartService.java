package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口 
 * @author Steven
 */
public interface CartService {

    /**
    * 添加商品到购物车
    * @param cartList 原来购物车列表
    * @param itemId skuId
    * @param num 购买数量
    * @return 添加后购物车列表
    */
   public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );
}
