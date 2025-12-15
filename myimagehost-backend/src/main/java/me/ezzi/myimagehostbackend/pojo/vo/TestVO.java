package me.ezzi.myimagehostbackend.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestVO {
    private Boolean testStatus;
    private String testMessage;
}
