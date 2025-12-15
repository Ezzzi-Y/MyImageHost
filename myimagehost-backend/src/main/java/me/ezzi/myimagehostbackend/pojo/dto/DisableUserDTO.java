package me.ezzi.myimagehostbackend.pojo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DisableUserDTO {
    
    @NotNull(message = "用户ID不能为空")

    private Long userId;          // 被禁用用户ID
    
    private String reason;        // 禁用原因
    
    private Integer durationDays; // 禁用天数（0 表示永久禁用）
}
