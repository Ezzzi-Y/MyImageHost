package me.ezzi.myimagehostbackend.controller;

import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.pojo.vo.TestVO;
import me.ezzi.myimagehostbackend.service.InformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/information")
@Slf4j
public class InformationController {

    @Autowired
    InformationService informationService;

    @GetMapping("/testInf")
    public Result<TestVO> getTestInf(){
        log.info("获取测试信息");
        return Result.success(informationService.getTestInf());
    }
}
