package me.ezzi.myimagehostbackend.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO {
    private Boolean testStatus;
    private String testMessage;
}
