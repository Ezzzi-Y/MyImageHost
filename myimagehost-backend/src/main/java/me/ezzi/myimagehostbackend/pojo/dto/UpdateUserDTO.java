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
public class UpdateUserDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;
    
    private Long quotaSpace;
    
    private Long quotaCount;

}
