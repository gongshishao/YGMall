package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import entity.PageResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class ContentServiceImpl implements ContentService {

    //引入log
    private Logger logger = Logger.getLogger(ContentServiceImpl.class);

    @Autowired
    private TbContentMapper contentMapper;
    //注入redis缓存模板
	@Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbContent> list = contentMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());
        return result;
    }

    /**
     * 增加广告，增加之后先删除缓存中该类型所有的广告，缓存需要再次访问时产生新的缓存
     */
    @Override
    public void add(TbContent content) {
        contentMapper.insertSelective(content);
        redisTemplate.boundHashOps("content").delete(content.getCategoryId());
    }

    /**
     * 修改广告，修改前后要删除之前及之后的缓存
     */
    @Override
    public void update(TbContent content) {
        //现获取之前的广告分类id
        Long categoryId = contentMapper.selectByPrimaryKey(content.getId()).getCategoryId();
        //先删除之前的广告缓存
        redisTemplate.boundHashOps("content").delete(categoryId);
        contentMapper.updateByPrimaryKeySelective(content);
        //如果改变了广告类型，则把新增的页删除
        if (categoryId.longValue() !=content.getCategoryId().longValue()){
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除，删除之前把缓存页删掉
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        List<TbContent> content = contentMapper.selectByExample(example);
        for (TbContent tbContent : content) {
            redisTemplate.boundHashOps("content").delete(tbContent.getCategoryId());
        }
        contentMapper.deleteByExample(example);
    }

    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageResult<TbContent> result = new PageResult<TbContent>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();

        if (content != null) {
            //如果字段不为空
            if (content.getTitle() != null && content.getTitle().length() > 0) {
                criteria.andLike("title", "%" + content.getTitle() + "%");
            }
            //如果字段不为空
            if (content.getUrl() != null && content.getUrl().length() > 0) {
                criteria.andLike("url", "%" + content.getUrl() + "%");
            }
            //如果字段不为空
            if (content.getPic() != null && content.getPic().length() > 0) {
                criteria.andLike("pic", "%" + content.getPic() + "%");
            }
            //如果字段不为空
            if (content.getStatus() != null && content.getStatus().length() > 0) {
                criteria.andLike("status", "%" + content.getStatus() + "%");
            }

        }

        //查询数据
        List<TbContent> list = contentMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbContent> info = new PageInfo<TbContent>(list);
        result.setTotal(info.getTotal());

        return result;
    }

    /**
     * 加载广告页，查询数据库之前通过redis查询缓存广告，如果没有再从数据库查询，然后存入缓存
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        //查询之前先从缓存查询
        List<TbContent> contents = (List<TbContent>) redisTemplate.boundHashOps("content").get(categoryId);
        if (contents == null) {
            //设置查询条件
            Example example = new Example(TbContent.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("categoryId", categoryId);
            //查询正常状态的数据
            criteria.andEqualTo("status", "1");
            //设置排序,多个字段可逗号分隔
            example.setOrderByClause("sortOrder asc");
            contents = contentMapper.selectByExample(example);
            //在将查询结果返回给视图解析时，先存入缓存中
            redisTemplate.boundHashOps("content").put(categoryId, contents);
        }else{
            //方便测试
            logger.info("从缓存中读取数据");
        }
        return contents;
    }

}
