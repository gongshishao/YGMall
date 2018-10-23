package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service(interfaceClass = GoodsService.class)
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbGoods> list = goodsMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 保存商品及sku,设计到的表有good,goodDesc,item
     */
    @Override
    public void add(Goods goods) {
        //1.保存商品主要信息
        goods.getGoods().setAuditStatus("0");//设置发布商品状态为未审核
        goodsMapper.insertSelective(goods.getGoods());
        //2.保存商品描述
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());//设置商品描述id
        goodsDescMapper.insertSelective(goods.getGoodsDesc());
        //3.保存sku ==>item
        saveItemList(goods);
    }

    /**
     * 抽取保存商品SKU代码
     * @param goods
     */
    private void saveItemList(Goods goods) {
        //勾选了启用规格蔡需要保存sku
        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
                //1.设置item表中的商品标题,要求商品名+属性规格(是一个key:value形式的json数据)
                String title = goods.getGoods().getGoodsName();
                Map<String, Object> map = JSON.parseObject(item.getSpec());
                Iterator<Map.Entry<String, Object>> iter = map.entrySet().iterator();//增强map遍历效率
                while (iter.hasNext()) {
                    //加上所有规格值 ==>attributeValue
                    title += " " + iter.next().getValue();
                }
                item.setTitle(title);//设置商品标题
                //3.设置item
                setItemValues(goods, item);
                //9.保存SKU
                itemMapper.insertSelective(item);
            }
        }else {//未勾选则设置默认规格
            TbItem item=new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品KPU+规格描述串作为SKU名称
            item.setPrice( goods.getGoods().getPrice() );//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            setItemValues(goods,item);
            itemMapper.insert(item);
        }
    }

    /**
     * 抽取设置item代码
     * @param goods
     * @param item
     */
    private void setItemValues(Goods goods, TbItem item) {
        //2.设置item表中的商品图片,spu的第一张(获取的goodsDesc是json数组形式)
        List<Map> imgList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imgList.size() > 0) {
            item.setImage(imgList.get(0).get("url").toString());//设置商品描述图片
        }
        //3.设置item表中商品类目id[非空]
        item.setCategoryid(goods.getGoods().getCategory3Id());
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());//分类名称
        //4.设置item表中创建日期
        item.setCreateTime(new Date());
        //5.设置更新日期
        item.setUpdateTime(new Date());
        //6.所属SPU商品id
        item.setGoodsId(goods.getGoods().getId());
        //7.设置item表中所属商家 ==>seller
        item.setSellerId(goods.getGoods().getSellerId());
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
        //8.设置item表中品牌信息 ==>brand
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        //1.修改过的商品，状态设置为未审核，重新审核一次
        goods.getGoods().setAuditStatus("0");//设置发布商品状态为未审核
        //修改商品基本信息
        goodsMapper.updateByPrimaryKeySelective(goods.getGoods());
        //2.修改商品描述信息
        goodsDescMapper.updateByPrimaryKeySelective(goods.getGoodsDesc());
        //3.更新sku ==>item,更新sku信息，更新前先删除原来的sku
        TbItem item = new TbItem();
        item.setGoodsId(goods.getGoods().getId());
        itemMapper.delete(item);
        //保存新的sku
        saveItemList(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(goodsDesc);
        //根据goodsId查询item
        TbItem item = new TbItem();
        item.setGoodsId(id);
        List<TbItem> items = itemMapper.select(item);
        goods.setItemList(items);
        return goods;
    }

    /**
     * 批量删除,逻辑删除,即Is_delete设置为1
     */
    @Override
    public void delete(Long[] ids) {
        //设置商品删除状态
        TbGoods record = new TbGoods();
        record.setIsDelete("1");
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件逻辑删除数据
        goodsMapper.updateByExampleSelective(record, example);
    }

    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageResult<TbGoods> result = new PageResult<TbGoods>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();

        if (goods != null) {
            //如果字段不为空
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andLike("sellerId", "%" + goods.getSellerId() + "%");
                criteria.andEqualTo("sellerId", goods.getSellerId());
            }
            //如果字段不为空
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andLike("goodsName", "%" + goods.getGoodsName() + "%");
            }
            //如果字段不为空
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andLike("auditStatus", "%" + goods.getAuditStatus() + "%");
            }
            //如果字段不为空
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andLike("isMarketable", "%" + goods.getIsMarketable() + "%");
            }
            //如果字段不为空
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andLike("caption", "%" + goods.getCaption() + "%");
            }
            //如果字段不为空
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andLike("smallPic", "%" + goods.getSmallPic() + "%");
            }
            //如果字段不为空
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andLike("isEnableSpec", "%" + goods.getIsEnableSpec() + "%");
            }

            /*if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andLike("isDelete", "%" + goods.getIsDelete() + "%");
            }*/
            //查询未删除的商品
            criteria.andIsNull("isDelete");


        }

        //查询数据
        List<TbGoods> list = goodsMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbGoods> info = new PageInfo<TbGoods>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 运营商审核商品,改变商品状态
     * @param ids --批量勾选的商品id
     * @param status --审核后的状态码
     * @return
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        //设置商品上架状态
        TbGoods record = new TbGoods();
        record.setAuditStatus(status);
        //数组转list
        List list = Arrays.asList(ids);
        //根据ids查询商品
        Example example = new Example(TbGoods.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", list);
        //根据条件更新状态
        goodsMapper.updateByExampleSelective(record, example);
    }

    /**
     * 根据SPU-ID列表和状态，查询SKU列表
     * @param goodsIds
     * @param status
     * @return
     */
    @Override
    public List<TbItem> findItemListByGoodsIdsAndStatus(Long[] goodsIds, String status) {
        Example example = new Example(TbItem.class);
        Example.Criteria criteria = example.createCriteria();
        //查询条件设置-spu列表
        List longs = Arrays.asList(goodsIds);
        criteria.andIn("goodsId", longs);
        //查询条件设置-商品状态
        criteria.andEqualTo("status", status);
        //查询结果
        List<TbItem> items = itemMapper.selectByExample(example);
        return items;
    }

}
