package me.ezzi.myimagehostbackend.controller;

import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.pojo.dto.DeleteImageDTO;
import me.ezzi.myimagehostbackend.pojo.dto.ImageDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateAliasDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Image;
import me.ezzi.myimagehostbackend.pojo.entity.PageResult;
import me.ezzi.myimagehostbackend.pojo.entity.Result;
import me.ezzi.myimagehostbackend.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public Result upload(ImageDTO imageDTO){
        log.info("用户上传{}", imageDTO);
        imageService.uploadFile(imageDTO);
        return Result.success();
    }

    @GetMapping("/listImage")
    public Result<PageResult> listImages(Integer page, Integer pageSize){
        log.info("用户查询文件列表");
        return Result.success(imageService.listImage(page, pageSize));
    }

    @DeleteMapping("/deleteImageBatch")
    public Result deleteBatch(@RequestBody DeleteImageDTO deleteImageDTO){
        log.info("用户批量删除文件：{}", deleteImageDTO);
        imageService.deleteBatchByIds(deleteImageDTO.getIds());
        return Result.success();
    }

    @PutMapping("/updateAlias")
    public Result updateAlias(@RequestBody UpdateAliasDTO updateAliasDTO){
        log.info("用户修改图片别名");
        imageService.updateAlias(updateAliasDTO);
        return Result.success();
    }

    @GetMapping("/searchImages")
    public Result<List<Image>> searchImage(String alias){
        log.info("用户根据别名搜索图片");
        return Result.success(imageService.searchImageByAlias(alias));
    }
}
