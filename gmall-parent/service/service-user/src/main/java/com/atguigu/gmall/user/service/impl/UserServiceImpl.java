package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
@SuppressWarnings("all")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    /**
     * 用户登录
     * @param userInfo
     * @return
     */
    @Override
    public UserInfo login(UserInfo userInfo) {
        //select *from userinfo where login_name=? and passwd=?

        //处理密码加密
        String newPass = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        //封装条件
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("login_name",userInfo.getLoginName());
        queryWrapper.eq("passwd",newPass);

        UserInfo user = userInfoMapper.selectOne(queryWrapper);


        return user;
    }

    /**
     * 查询用户地址列表
     * @param userId
     * @return
     */
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {

        //select*from user_address where user_id=?
        //封装查询条件
        QueryWrapper<UserAddress> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);

        return userAddressMapper.selectList(queryWrapper);
    }
}
