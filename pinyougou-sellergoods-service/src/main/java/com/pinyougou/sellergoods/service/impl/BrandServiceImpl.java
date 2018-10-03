package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 品牌模块服务层实现类
 */
@Service
public class BrandServiceImpl implements BrandService {

    //注入mapper数据访问对象
    @Autowired
    private TbBrandMapper brandMapper;

    /**
     * 查询所有品牌数据
     * @return
     */
    @Override
    public List<TbBrand> findAll() {
        return brandMapper.selectByExample(null);
    }

    /**
     * 查询分页列表
     * @param pageNum   --当前页码
     * @param pageSize  --每页数量
     * @return
     */
    @Override
    public PageResult findByPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);                                 //pagehelper    mybatis分页插件,在数据访问层dao引入的插件
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);//Page   mybatis分页对象
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 新增品牌
     * @param brand
     */
    @Override
    public void addBrand(TbBrand brand) {
        brandMapper.insert(brand);
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @Override
    public TbBrand findOne(Long id) {
        return brandMapper.selectByPrimaryKey(id);
    }

    /**
     * 修改品牌信息
     * @param brand
     */
    @Override
    public void updateById(TbBrand brand) {
        brandMapper.updateByPrimaryKey(brand);
    }

    /**
     *
     * @param ids -- ids数组
     */
    @Override
    public void deleteByIds(Long[] ids) {
        for (Long id : ids) {
            brandMapper.deleteByPrimaryKey(id);
        }
    }

    @Override
    public PageResult findByPage(TbBrand brand, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize); //pagehelper    mybatis分页插件,在数据访问层dao引入的插件
        TbBrandExample example = new TbBrandExample();//创建实例对象
        Criteria criteria = example.createCriteria();
        if (brand!=null) {
            if (brand.getName() != null && brand.getName().length() > 0) {
                criteria.andNameLike("%"+brand.getName()+"%");//添加名字模糊查询
            }
            if (brand.getFirstChar() != null && brand.getFirstChar().length() > 0) {
                criteria.andFirstCharLike("%" + brand.getFirstChar()+"%");
            }
        }
        Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);//Page   mybatis分页对象
        return new PageResult(page.getTotal(),page.getResult());
    }
}
