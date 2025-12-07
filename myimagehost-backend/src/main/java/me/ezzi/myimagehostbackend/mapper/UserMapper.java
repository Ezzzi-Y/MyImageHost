package me.ezzi.myimagehostbackend.mapper;

import me.ezzi.myimagehostbackend.pojo.dto.ForgetPasswordDTO;
import me.ezzi.myimagehostbackend.pojo.dto.LoginDTO;
import me.ezzi.myimagehostbackend.pojo.dto.RegisterDTO;
import me.ezzi.myimagehostbackend.pojo.entity.User;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from users where email = #{email} and password = #{password}")
    User findUserByEmailAndPassword(LoginDTO loginDTO);

    @Select("select count(*) from users where email = #{email}")
    int countByEmail(String email);

    @Select("select * from users")
    List<UserVO> list();

    @Select("select role from users where id = #{id}")
    String getRoleById(Long id);

    @Insert("insert into users (email, nickname, password) values (#{email},#{nickname},#{password})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertUser(RegisterDTO registerDTO);

    @Delete("delete from users where id = #{id}")
    void deleteUserById(Long id);

    @Update("UPDATE users SET enabled = TRUE, disabled_until = NULL WHERE enabled = FALSE AND disabled_until <= NOW()")
    void recoverUsers();

    @Update("update users set enabled = false,disable_reason = #{disableReason},disabled_until = #{disabledUntil} where id = #{id}")
    void disableUser(Long id, String disableReason, LocalDateTime disabledUntil);

    @Update("update users set enabled = true,disabled_until = NULL,disable_reason = NULL where id = #{id}")
    void enableUserById(Long id);

    @Update("update users set password = #{password} where email = #{email}")
    void updatePassword(ForgetPasswordDTO forgetPasswordDTO);

    void updateUser(User user);

    List<UserVO> search(User user);

    void increaseUsedSpace(Long userId,Long size);

    void decreaseUsedSpace(Long userId, Long size, Integer count);

    Long countByUrl(String url);
}
