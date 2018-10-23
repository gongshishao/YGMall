package com.pinyougou.page.service;

/**
 * FreeMarker静态化页面
 * 商品详细页接口
 * @author Steven
 *
 */
public interface ItemPageService {
    /**
    * 生成商品详细页
    * @param goodsId
    */
   public boolean genItemHtml(Long goodsId);
}
