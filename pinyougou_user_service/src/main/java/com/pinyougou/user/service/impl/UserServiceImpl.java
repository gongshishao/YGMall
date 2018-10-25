package com.pinyougou.user.service.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.abel533.entity.Example;
import com.github.pagehelper.PageInfo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.*;

/**
 * 用户模块
 * 业务逻辑实现
 *
 * @author Steven
 */
@Service
public class UserServiceImpl implements UserService {
    private Logger logger = Logger.getLogger(UserServiceImpl.class);

    @Autowired
    private TbUserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination smsDestination;

    @Value("${template_code}")
    private String template_code;

    @Value("${sign_name}")
    private String sign_name;


    /**
     * 用户注册
     */
    @Override
    public void add(TbUser user) {
        user.setCreated(new Date());//创建日期
        user.setUpdated(user.getCreated());//修改日期
        String password = DigestUtils.md5Hex(user.getPassword());//对密码加密
        user.setPassword(password);
        userMapper.insertSelective(user);
    }

    /**
     * 生成用户短信验证码
     * @param phone
     */
    @Override
    public void createSmsCode(final String phone) {
        //生成6位随机数
        final String code = ((long)(Math.random() * 1000000)) + "";
        logger.info("验证码：" + code);
        //将验证码放入redis
        redisTemplate.boundHashOps("smscode").put(phone, code);
        redisTemplate.expire("smscode", 3600, TimeUnit.SECONDS);
        //发送activemq...
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);//手机号
                mapMessage.setString("template_code", template_code);//模板编号
                mapMessage.setString("sign_name", sign_name);//签名
                Map m=new HashMap<>();
                m.put("code", code);
                mapMessage.setString("param", JSON.toJSONString(m));//参数
                return mapMessage;
            }
        });
    }

    /**
     * 判断验证码是否存在
     * @param phone
     * @param code
     * @return
     */
    @Override
    public boolean checkSmsCode(String phone, String code) {
        //读取验证码
        String smscode = (String) redisTemplate.boundHashOps("smscode").get(phone);
        //验证验证码
        if(smscode == null || !smscode.equals(code)){
            return false;
        }
        return true;
    }

    /**
     * 查询全部
     */
    @Override
    public List<TbUser> findAll() {
        return userMapper.select(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {

        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //查询数据
        List<TbUser> list = userMapper.select(null);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());
        return result;
    }




    /**
     * 修改
     */
    @Override
    public void update(TbUser user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbUser findOne(Long id) {
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        //数组转list
        List longs = Arrays.asList(ids);
        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", longs);

        //跟据查询条件删除数据
        userMapper.deleteByExample(example);
    }

    @Override
    public PageResult findPage(TbUser user, int pageNum, int pageSize) {
        PageResult<TbUser> result = new PageResult<TbUser>();
        //设置分页条件
        PageHelper.startPage(pageNum, pageSize);

        //构建查询条件
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();

        if (user != null) {
            //如果字段不为空
            if (user.getUsername() != null && user.getUsername().length() > 0) {
                criteria.andLike("username", "%" + user.getUsername() + "%");
            }
            //如果字段不为空
            if (user.getPassword() != null && user.getPassword().length() > 0) {
                criteria.andLike("password", "%" + user.getPassword() + "%");
            }
            //如果字段不为空
            if (user.getPhone() != null && user.getPhone().length() > 0) {
                criteria.andLike("phone", "%" + user.getPhone() + "%");
            }
            //如果字段不为空
            if (user.getEmail() != null && user.getEmail().length() > 0) {
                criteria.andLike("email", "%" + user.getEmail() + "%");
            }
            //如果字段不为空
            if (user.getSourceType() != null && user.getSourceType().length() > 0) {
                criteria.andLike("sourceType", "%" + user.getSourceType() + "%");
            }
            //如果字段不为空
            if (user.getNickName() != null && user.getNickName().length() > 0) {
                criteria.andLike("nickName", "%" + user.getNickName() + "%");
            }
            //如果字段不为空
            if (user.getName() != null && user.getName().length() > 0) {
                criteria.andLike("name", "%" + user.getName() + "%");
            }
            //如果字段不为空
            if (user.getStatus() != null && user.getStatus().length() > 0) {
                criteria.andLike("status", "%" + user.getStatus() + "%");
            }
            //如果字段不为空
            if (user.getHeadPic() != null && user.getHeadPic().length() > 0) {
                criteria.andLike("headPic", "%" + user.getHeadPic() + "%");
            }
            //如果字段不为空
            if (user.getQq() != null && user.getQq().length() > 0) {
                criteria.andLike("qq", "%" + user.getQq() + "%");
            }
            //如果字段不为空
            if (user.getIsMobileCheck() != null && user.getIsMobileCheck().length() > 0) {
                criteria.andLike("isMobileCheck", "%" + user.getIsMobileCheck() + "%");
            }
            //如果字段不为空
            if (user.getIsEmailCheck() != null && user.getIsEmailCheck().length() > 0) {
                criteria.andLike("isEmailCheck", "%" + user.getIsEmailCheck() + "%");
            }
            //如果字段不为空
            if (user.getSex() != null && user.getSex().length() > 0) {
                criteria.andLike("sex", "%" + user.getSex() + "%");
            }

        }

        //查询数据
        List<TbUser> list = userMapper.selectByExample(example);
        //保存数据列表
        result.setRows(list);

        //获取总记录数
        PageInfo<TbUser> info = new PageInfo<TbUser>(list);
        result.setTotal(info.getTotal());

        return result;
    }

}
