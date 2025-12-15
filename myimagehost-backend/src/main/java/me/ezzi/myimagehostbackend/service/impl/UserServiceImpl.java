package me.ezzi.myimagehostbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.mapper.UserMapper;
import me.ezzi.myimagehostbackend.pojo.dto.*;
import me.ezzi.myimagehostbackend.pojo.entity.User;
import me.ezzi.myimagehostbackend.pojo.vo.QuotaVO;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import me.ezzi.myimagehostbackend.service.ImageService;
import me.ezzi.myimagehostbackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j

public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;



    @Override
    public void upload(Long size) {
        Long userId = StpUtil.getLoginIdAsLong();
        userMapper.increaseUsedSpace(userId, size);
    }

    @Override
    public void delete(Long size, Integer count) {
        Long userId = StpUtil.getLoginIdAsLong();
        userMapper.decreaseUsedSpace(userId, size, count);
    }

    @Override
    public QuotaVO getQuota() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserVO> list = userMapper.search(User.builder().id(userId).build());

        if (CollectionUtils.isEmpty(list) || list.size() > 1) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }

        QuotaVO quotaVO = new QuotaVO();
        BeanUtils.copyProperties(list.get(0), quotaVO);
        return quotaVO;
    }
}

