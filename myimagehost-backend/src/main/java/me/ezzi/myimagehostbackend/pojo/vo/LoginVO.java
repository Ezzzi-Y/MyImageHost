package me.ezzi.myimagehostbackend.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {
    private String email;
    private String nickname;
    private String role;
    private String token;

    private Long quotaSpace;
    private Long quotaCount;
    private Long usedSpace;
    private Long usedCount;

}
