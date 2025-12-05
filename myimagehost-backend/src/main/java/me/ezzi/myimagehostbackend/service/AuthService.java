package me.ezzi.myimagehostbackend.service;

import me.ezzi.myimagehostbackend.pojo.dto.ForgetPasswordDTO;
import me.ezzi.myimagehostbackend.pojo.dto.LoginDTO;
import me.ezzi.myimagehostbackend.pojo.dto.RegisterDTO;
import me.ezzi.myimagehostbackend.pojo.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginDTO loginDTO);

    LoginVO register(RegisterDTO registerDTO);

    void forgetPassword(ForgetPasswordDTO forgetPasswordDTO);
}
