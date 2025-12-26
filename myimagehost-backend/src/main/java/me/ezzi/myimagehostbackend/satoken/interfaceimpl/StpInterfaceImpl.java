package me.ezzi.myimagehostbackend.satoken.interfaceimpl;

import cn.dev33.satoken.stp.StpInterface;
import me.ezzi.myimagehostbackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限加载接口实现类
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private UserMapper userMapper;

    /**
     *
     * @param loginId  账号id
     * @param loginType 账号类型
     * @return
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {

        long uid = Long.parseLong(loginId.toString());
        String role = userMapper.getRoleById(uid);
        return List.of(role);
    }


}
