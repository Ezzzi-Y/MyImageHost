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
public class Image {

    private Long id;
    private Long userId;

    private String originalName;
    private String alias;
    private String url;
    private String md5;
    private Long size;

    private LocalDateTime createTime;

}
