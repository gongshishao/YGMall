package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

import java.util.List;

/**
 * 品牌模块服务层接口
 */
public interface BrandService {

    /**
     * 查询所有品牌数据
     * @return
     */
    public List<TbBrand> findAll();

    /**
     * 查询返回分页列表
     * @param pageNum   --当前页码
     * @param pageSize  --每页数量
     * @return
     */
    public PageResult findByPage(int pageNum,int pageSize);

    /**
     * 增加品牌数据
     * @param brand
     */
    public void addBrand(TbBrand brand);

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    TbBrand findOne(Long id);
}
