package me.ezzi.myimagehostbackend.service;


import me.ezzi.myimagehostbackend.pojo.dto.*;
import me.ezzi.myimagehostbackend.pojo.vo.QuotaVO;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;

import java.util.List;

public interface UserService {

    List<UserVO> list();

    void deleteUserById(Long id);

    void disableUser(DisableUserDTO dto);

    void enableUser(Long id);

    void updateUser(UpdateUserDTO updateUserDTO);

    List<UserVO> searchList(SearchUserDTO searchUserDTO);

    UserVO getUserById(Long id);

    void upload(Long Size);

    QuotaVO getQuota();
}
