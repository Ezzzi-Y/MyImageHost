package me.ezzi.myimagehostbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.mapper.UserMapper;
import me.ezzi.myimagehostbackend.pojo.dto.DisableUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.SearchUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.TestDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateUserDTO;
import me.ezzi.myimagehostbackend.pojo.entity.User;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import me.ezzi.myimagehostbackend.service.AdminService;
import me.ezzi.myimagehostbackend.service.ImageService;
import me.ezzi.myimagehostbackend.service.InformationService;
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
public class AdminServiceImpl implements AdminService {

    @Autowired
    private InformationService informationService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    @Lazy
    private ImageService imageService;

    @Override
    public List<UserVO> list() {
        return userMapper.list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUserById(Long userId) {
        String role = userMapper.getRoleById(userId);
        if ("ADMIN".equals(role)) {
            throw new IllegalArgumentException("不得删除管理员用户");
        }

        imageService.deleteByUserId(userId);
        userMapper.deleteUserById(userId);
    }

    @Override
    public void disableUser(DisableUserDTO dto) {
        String role = userMapper.getRoleById(dto.getUserId());
        if (role == null) throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        if ("ADMIN".equals(role)) throw new BaseException("不能禁用管理员用户");

        LocalDateTime until = null;
        if (dto.getDurationDays() != null && dto.getDurationDays() > 0) {
            until = LocalDateTime.now().plusDays(dto.getDurationDays());
        }

        userMapper.disableUser(dto.getUserId(), dto.getReason(), until);
        StpUtil.kickout(dto.getUserId());
    }

    @Override
    public void enableUser(Long id) {
        userMapper.enableUserById(id);
    }

    @Override
    public void updateUser(UpdateUserDTO updateUserDTO) {
        User user = new User();
        BeanUtils.copyProperties(updateUserDTO, user);
        userMapper.updateUser(user);
    }

    @Override
    public List<UserVO> searchList(SearchUserDTO searchUserDTO) {
        User user = new User();
        BeanUtils.copyProperties(searchUserDTO, user);
        return userMapper.search(user);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = User.builder().id(id).build();
        List<UserVO> search = userMapper.search(user);

        if (CollectionUtils.isEmpty(search)) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (search.size() > 1) {
            throw new BaseException(MessageConstant.UNKNOWN_ERROR);
        }
        return search.get(0);
    }

    @Override
    public void updateTestStatus(TestDTO testDTO) {
        if (testDTO.getTestStatus()){
            informationService.startTest(testDTO.getTestMessage());
        }else{
            informationService.stopTest();
        }

    }
}
