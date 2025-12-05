package me.ezzi.myimagehostbackend.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuotaVO {
    private Long quotaSpace;
    private Long quotaCount;
    private Long usedSpace;
    private Long usedCount;
}
