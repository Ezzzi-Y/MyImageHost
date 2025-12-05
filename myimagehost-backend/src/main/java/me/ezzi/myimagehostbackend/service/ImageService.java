package me.ezzi.myimagehostbackend.service;

import me.ezzi.myimagehostbackend.pojo.dto.ImageDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateAliasDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Image;
import me.ezzi.myimagehostbackend.pojo.entity.PageResult;

import java.util.List;

public interface ImageService {

    /**
     * 上传图片
     * @param imageDTO 图片信息
     */
    void uploadFile(ImageDTO imageDTO);

    /**
     * 分页查询图片列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult listImage(Integer page, Integer pageSize);

    /**
     * 批量删除图片
     * @param ids 图片ID列表
     */
    void deleteBatchByIds(List<Long> ids);

    /**
     * 修改图片别名
     * @param updateAliasDTO 修改信息
     */
    void updateAlias(UpdateAliasDTO updateAliasDTO);

    /**
     * 根据别名搜索图片
     * @param alias 别名
     * @return 图片列表
     */
    List<Image> searchImageByAlias(String alias);

    /**
     * 删除指定用户的所有图片
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
}
