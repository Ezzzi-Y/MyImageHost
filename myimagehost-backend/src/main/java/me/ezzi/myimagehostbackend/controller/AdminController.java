package me.ezzi.myimagehostbackend.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.dto.DisableUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.SearchUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.TestDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateUserDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import me.ezzi.myimagehostbackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
@SaCheckRole("ADMIN")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/list")
    public Result<List<UserVO>> list() {
        log.info("管理员查询用户列表");
        return Result.success(adminService.list());
    }

    @DeleteMapping("/deleteUser/{id}")
    public Result deleteUser(@PathVariable Long id) {
        log.info("管理员删除用户：{}", id);
        adminService.deleteUserById(id);
        return Result.success();
    }

    @PostMapping("/disableUser")
    public Result disableUser(@RequestBody @Validated DisableUserDTO dto) {
        log.info("管理员封禁用户: {}, 天数: {}", dto.getUserId(), dto.getDurationDays());
        adminService.disableUser(dto);
        return Result.success();
    }

    @PutMapping("/enableUser/{id}")
    public Result enableUser(@PathVariable Long id) {
        log.info("管理员启用用户：{}", id);
        adminService.enableUser(id);
        return Result.success();
    }

    @PostMapping("/updateSpace")
    public Result updateUser(@RequestBody @Validated UpdateUserDTO updateUserDTO) {
        log.info("管理员修改用户配额：{}", updateUserDTO);
        adminService.updateUser(updateUserDTO);
        return Result.success();
    }

    @PostMapping("/searchUser")
    public Result<List<UserVO>> searchList(@RequestBody SearchUserDTO searchUserDTO) {
        log.info("管理员搜索用户：{}", searchUserDTO);
        return Result.success(adminService.searchList(searchUserDTO));
    }

    @GetMapping("/getUser/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        log.info("管理员根据ID查询用户：{}", id);
        return Result.success(adminService.getUserById(id));
    }

    @PutMapping("/updateTestStatus")
    public Result updateTestStatus(@RequestBody TestDTO testDTO){
        log.info("管理员修改用户测试状态{}",testDTO);
        adminService.updateTestStatus(testDTO);
        return Result.success();
    }

}
