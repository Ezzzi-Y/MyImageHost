package me.ezzi.myimagehostbackend.service;

import me.ezzi.myimagehostbackend.pojo.dto.DisableUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.SearchUserDTO;
import me.ezzi.myimagehostbackend.pojo.dto.TestDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateUserDTO;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;

import java.util.List;

public interface AdminService {

    List<UserVO> list();

    void deleteUserById(Long id);

    void disableUser(DisableUserDTO dto);

    void enableUser(Long id);

    void updateUser(UpdateUserDTO updateUserDTO);

    List<UserVO> searchList(SearchUserDTO searchUserDTO);

    UserVO getUserById(Long id);

    void updateTestStatus(TestDTO testDTO);
}
