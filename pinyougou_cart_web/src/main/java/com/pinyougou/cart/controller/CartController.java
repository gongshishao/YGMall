package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.CookieUtil;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 购物车控制层
 */
@RestController
@RequestMapping("cart")
public class CartController {
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;
    /**
     * 查询购物车列表
     * @return
     */
    @RequestMapping("findCartList")
    public List<Cart> findCartList() {
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", true);
        //如果购物车cookies为空
        if (StringUtils.isBlank(cartListStr)) {
            //返回个List对象，便于后续逻辑调用
            return new ArrayList<>();
        } else {
            //把json串转成list
            List<Cart> carts = JSON.parseArray(cartListStr, Cart.class);
            return carts;
        }
    }

    /**
     * 添加商品到购物车
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    public Result addGoodsToCartList(Long itemId, Integer num) {
        try {
            List<Cart> cartList = findCartList();
            //注意，这里要重新接收一下，这里是调用接口查询数据
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            //存一天
            CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, true);
            return new Result(true, "保存成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "保存失败");
        }
    }
}
