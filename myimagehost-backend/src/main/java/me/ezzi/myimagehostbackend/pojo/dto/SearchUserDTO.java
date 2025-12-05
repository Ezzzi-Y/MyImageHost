package me.ezzi.myimagehostbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchUserDTO {
    private String nickname;
    private String email;
    private Boolean enabled;
}
