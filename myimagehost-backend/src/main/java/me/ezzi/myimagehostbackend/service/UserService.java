package me.ezzi.myimagehostbackend.service;


import me.ezzi.myimagehostbackend.pojo.dto.*;
import me.ezzi.myimagehostbackend.pojo.vo.QuotaVO;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;

import java.util.List;

public interface UserService {


    void upload(Long Size);

    void delete(Long size, Integer count);

    QuotaVO getQuota();
}
