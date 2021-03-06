package com.pinyougou.user.service;

import java.util.List;

import com.pinyougou.pojo.TbUser;

import entity.PageResult;

/**
 * 用户模块
 * 业务逻辑接口
 *
 * @author Steven
 */
public interface UserService {

    /**
     * 用户注册
     */
    public void add(TbUser user);

    /**
     * 生成短信验证码
     *
     * @return
     */
    public void createSmsCode(String phone);

    /**
     * 判断短信验证码是否存在
     * @param phone
     * @return
     */
    public boolean  checkSmsCode(String phone,String code);


    /**
     * 返回全部列表
     *
     * @return
     */
    public List<TbUser> findAll();

    /**
     * 返回分页列表
     *
     * @return
     */
    public PageResult findPage(int pageNum, int pageSize);

    /**
     * 修改
     */
    public void update(TbUser user);

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    public TbUser findOne(Long id);

    /**
     * 批量删除
     *
     * @param ids
     */
    public void delete(Long[] ids);

    /**
     * 分页
     *
     * @param pageNum  当前页 码
     * @param pageSize 每页记录数
     * @return
     */
    public PageResult findPage(TbUser user, int pageNum, int pageSize);

}
