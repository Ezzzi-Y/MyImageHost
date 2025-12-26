package me.ezzi.myimagehostbackend.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.annotation.FeatureSwitch;
import me.ezzi.myimagehostbackend.pojo.dto.ForgetPasswordDTO;
import me.ezzi.myimagehostbackend.pojo.dto.LoginDTO;
import me.ezzi.myimagehostbackend.pojo.dto.RegisterDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.pojo.vo.LoginVO;
import me.ezzi.myimagehostbackend.service.AuthService;
import me.ezzi.myimagehostbackend.service.EmailService;
import me.ezzi.myimagehostbackend.service.impl.InformationServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailService emailService;
    @Autowired
    private InformationServiceImpl informationServiceImpl;

    @GetMapping("/email/verification")
    @FeatureSwitch(value = "user.email.send", description = "发送验证码邮件")
    public Result getVerificationCode(
            @RequestParam @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
            @RequestParam(required = false, defaultValue = "register") String type){
        log.info("请求发送验证码至: {}, 类型: {}", email, type);
        emailService.sendVerificationCode(email, type);
        return Result.success();
    }

    @PostMapping("/login")
    @FeatureSwitch(value = "user.login", description = "用户登录")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO){
        log.info("用户登录");
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/register")
    @FeatureSwitch(value = "user.register", description = "用户注册")
    public Result<LoginVO> register(@RequestBody @Validated RegisterDTO registerDTO){
        log.info("用户注册");
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/forgetPassword")
    @FeatureSwitch(value = "user.reset.password", description = "用户重置密码")
    public Result forgetPassword(@RequestBody @Validated ForgetPasswordDTO forgetPasswordDTO){
        log.info("用户忘记密码");
        authService.forgetPassword(forgetPasswordDTO);
        return Result.success();
    }


}
