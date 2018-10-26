package com.pinyougou.cart.service;

import com.pinyougou.pojogroup.Cart;

import java.util.List;

/**
 * 购物车服务接口,从redis中查询是否有购物车并进行购物车合并，删除未登录时存在cookie中的购物车信息
 * @author Steven
 */
public interface CartService {

    /**
     * 从redis中查询购物车
     * @return
     */
    public List<Cart> findCartListFromRedis(String username);

    /**
     * 将购物车保存到redis
     */
    public void saveCartListToRedis(String username,List<Cart> cartList);


    /**
    * 添加商品到购物车
    * @param cartList 原来购物车列表
    * @param itemId skuId
    * @param num 购买数量
    * @return 添加后购物车列表
    */
   public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );

    /**
     * 合并购物车
     * @param cartList1
     * @param cartList2
     * @return
     */
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);


}
