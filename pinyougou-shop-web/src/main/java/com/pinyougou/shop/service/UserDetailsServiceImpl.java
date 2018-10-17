package com.pinyougou.shop.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义扩展权限认证类
 *
 * @author admin13
 * @version 1.0
 * @description com.pinyougou.service
 * @date 2018/10/10
 */

public class UserDetailsServiceImpl implements UserDetailsService {

    //引入日志
    private Logger logger = Logger.getLogger(SellerService.class);
    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info(username + ",进入了loadUserByUsername方法");
        //构造用户的角色列表
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        TbSeller seller = sellerService.findOne(username);
        //若登录的用户为已审核的商家,即状态为1,才进一步把User返回给security框架进行校验
        if (seller != null && "1".equals(seller.getStatus())) {
            //返回真实存在的用户，让Security框架对配置用户与密码信息是否匹配
            return new User(username, seller.getPassword(), authorities);
        } else {
            logger.info("商家登录失败,原因可能是非法登录:"+Exception.class);
            return null;
        }
    }
}
