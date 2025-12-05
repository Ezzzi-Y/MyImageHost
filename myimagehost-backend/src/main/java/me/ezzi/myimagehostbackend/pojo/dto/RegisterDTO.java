package me.ezzi.myimagehostbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDTO {

    private Long id;
    private String email;
    private String password;
    private String nickname;
    private String verificationCode;
}
