package me.ezzi.myimagehostbackend.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.dto.ForgetPasswordDTO;
import me.ezzi.myimagehostbackend.pojo.dto.LoginDTO;
import me.ezzi.myimagehostbackend.pojo.dto.RegisterDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.pojo.vo.LoginVO;
import me.ezzi.myimagehostbackend.service.AuthService;
import me.ezzi.myimagehostbackend.service.EmailService;
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

    @GetMapping("/email/verification")
    public Result getVerificationCode(@RequestParam @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email){
        log.info("请求发送验证码至: {}", email);
        emailService.sendVerificationCode(email);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO){
        log.info("用户登录");
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody @Validated RegisterDTO registerDTO){
        log.info("用户注册");
        LoginVO loginVO = authService.register(registerDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/forgetPassword")
    public Result forgetPassword(@RequestBody @Validated ForgetPasswordDTO forgetPasswordDTO){
        log.info("用户忘记密码");
        authService.forgetPassword(forgetPasswordDTO);
        return Result.success();
    }
}
