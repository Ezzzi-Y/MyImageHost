package me.ezzi.myimagehostbackend.controller;

import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.pojo.vo.QuotaVO;
import me.ezzi.myimagehostbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/quota")
    public Result<QuotaVO> quote(){
        log.info("用户查询配额");
        return Result.success(userService.getQuota());
    }

    @PostMapping("/updateEmail")
    public Result updateEmail(){
        log.info("用户更换绑定邮箱");
        return Result.success();
    }

}
