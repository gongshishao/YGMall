package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.CookieUtil;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
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
    private Logger logger = Logger.getLogger(CartController.class);

    //未避免远程服务超市，设置超时时间为6s（默认是1s）
    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 查询购物车列表,先判断是否为匿名用户，
     * 是则从cookie中获取数据，已登录则从redis中取出数据,并与cookie中的数据合并
     *
     * @return
     */
    @RequestMapping("findCartList")
    public List<Cart> findCartList() {
        //获取当前访客登录名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Cart> carts = new ArrayList<>();
        String cartListStr = CookieUtil.getCookieValue(request, "cartList", true);
        //如果购物车cookies为空
        if (StringUtils.isNotBlank(cartListStr)) {
            //把json串转成list
            carts = JSON.parseArray(cartListStr, Cart.class);
        }
        if ("anonymousUser".equals(username)) {//如果当前访客为匿名用户，则从cookie中取出数据
            logger.info("用户未登录，从cookie中取出数据");
            return carts;
        } else {//否则用户已登录，则从redis中取出购物车数据,并与cookie中的数据合并
            List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
            //从redis取出数据后，先判断cookie中是否有数据，有则合并
            if (carts.size() > 0) {//若cookie中没有数据则直接返回
                return cartListFromRedis;
            } else {//有则合并，并将新的数据存入redis
                List<Cart> mergeCartList = cartService.mergeCartList(carts, cartListFromRedis);
                cartService.saveCartListToRedis(username, mergeCartList);
                return mergeCartList;
            }
        }

    }

    /**
     * 添加商品到购物车，如果为匿名用户则存到cookie中，
     * 若已登录则存到redis中
     *
     * @param itemId
     * @param num
     * @return
     */
    @RequestMapping("addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:8085",allowCredentials = "true")//设置要访问该方法的域地址，允许凭证（cookie可以不写）
    public Result addGoodsToCartList(Long itemId, Integer num) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Cart> cartList = findCartList();
            //注意，这里要重新接收一下，这里是调用接口查询数据
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(username)) {//如果用户未登录
                logger.info("用户未登录，购物车信息将存入cookie保存一天");
                //存一天
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, true);
            } else {//已登录则从redis中取数据
                cartService.saveCartListToRedis(username, cartList);
            }
            return new Result(true, "保存成功");
        } catch (Exception e) {
            logger.error("保存购物车失败，原因是：" + e);
            return new Result(false, "保存失败");
        }
    }
}
