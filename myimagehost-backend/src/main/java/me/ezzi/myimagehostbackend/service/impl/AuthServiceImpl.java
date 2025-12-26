package me.ezzi.myimagehostbackend.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.bloomfilter.BitMapBloomFilter;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.exception.LoginFailException;
import me.ezzi.myimagehostbackend.exception.RegisterException;
import me.ezzi.myimagehostbackend.mapper.UserMapper;
import me.ezzi.myimagehostbackend.pojo.dto.ForgetPasswordDTO;
import me.ezzi.myimagehostbackend.pojo.dto.LoginDTO;
import me.ezzi.myimagehostbackend.pojo.dto.RegisterDTO;
import me.ezzi.myimagehostbackend.pojo.entity.User;
import me.ezzi.myimagehostbackend.pojo.vo.LoginVO;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import me.ezzi.myimagehostbackend.service.AuthService;
import me.ezzi.myimagehostbackend.service.EmailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailService emailService;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        String password = DigestUtils.md5DigestAsHex(loginDTO.getPassword().getBytes());
        loginDTO.setPassword(password);

        User user = userMapper.findUserByEmailAndPassword(loginDTO);
        if (user == null) {
            throw new LoginFailException(MessageConstant.LOGIN_FAILED);
        }

        log.info("登录中：{}", user);
        if (!user.getEnabled()) {
            throw new LoginFailException(MessageConstant.ACCOUNT_DISABLED + "被禁用原因：" + user.getDisableReason() + "。解除禁用时间：" + user.getDisabledUntil().format(formatter) + "。解禁会有一定延时~");
        }

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(tokenInfo.tokenValue);
        return loginVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginVO register(RegisterDTO registerDTO) {
        Assert.hasText(registerDTO.getEmail(), MessageConstant.LACK_PARAM);
        Assert.hasText(registerDTO.getPassword(), MessageConstant.LACK_PARAM);
        Assert.hasText(registerDTO.getNickname(), MessageConstant.LACK_PARAM);

        boolean isValid = emailService.validVerificationCode(registerDTO.getEmail(), registerDTO.getVerificationCode());
        if (!isValid) throw new RegisterException(MessageConstant.LACK_VALID);

        String password = DigestUtils.md5DigestAsHex(registerDTO.getPassword().getBytes());
        registerDTO.setPassword(password);

        userMapper.insertUser(registerDTO);

        LoginDTO loginDTO = LoginDTO.builder()
                .email(registerDTO.getEmail())
                .password(password)
                .build();

        User user = userMapper.findUserByEmailAndPassword(loginDTO);

        StpUtil.login(user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginVO loginVO = new LoginVO();
        BeanUtils.copyProperties(user, loginVO);
        loginVO.setToken(tokenInfo.tokenValue);
        return loginVO;
    }

    @Override
    public void forgetPassword(ForgetPasswordDTO forgetPasswordDTO) {
        if (!emailService.validVerificationCode(forgetPasswordDTO.getEmail(), forgetPasswordDTO.getVerificationCode())) {
            throw new BaseException(MessageConstant.LACK_VALID);
        }

        List<UserVO> list = userMapper.search(User.builder().email(forgetPasswordDTO.getEmail()).build());
        if (CollectionUtils.isEmpty(list)) throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        if (list.size() > 1) throw new BaseException(MessageConstant.UNKNOWN_ERROR);

        String password = DigestUtils.md5DigestAsHex(forgetPasswordDTO.getPassword().getBytes());
        forgetPasswordDTO.setPassword(password);
        userMapper.updatePassword(forgetPasswordDTO);
        StpUtil.kickout(list.get(0).getId());
    }



}
