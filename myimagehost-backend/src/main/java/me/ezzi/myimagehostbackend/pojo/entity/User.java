package me.ezzi.myimagehostbackend.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Long id;
    private String nickname;
    private String email;
    private String password;

    private String role;
    private Boolean enabled;
    private Long quotaSpace;
    private Long quotaCount;
    private Long usedSpace;
    private Long usedCount;

    private LocalDateTime disabledUntil;
    private String disableReason;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
