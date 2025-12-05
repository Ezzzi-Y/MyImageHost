package me.ezzi.myimagehostbackend.mapper;

import com.github.pagehelper.Page;
import me.ezzi.myimagehostbackend.pojo.entity.Image;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ImageMapper {

    @Delete("DELETE FROM images WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);

    @Delete("DELETE FROM images WHERE url = #{url}")
    void deleteByUrl(String url);

    @Delete("DELETE FROM images WHERE id = #{id}")
    void deleteById(Long id);

    @Insert("INSERT INTO images (user_id, original_name, alias, url, md5, size) " +
            "VALUES (#{userId}, #{originalName}, #{alias}, #{url}, #{md5}, #{size})")
    void insert(Image image);

    @Select("SELECT * FROM images WHERE user_id = #{userId} order by create_time desc")
    Page<Image> pageList(Long userId);

    // 批量删除（按图片 id）
    void deleteBatch(List<Long> ids);

    // 批量查询（按图片 id）
    List<Image> queryBatch(List<Long> ids);

    // 按用户id查询用户所有图片
    @Select("SELECT * FROM images WHERE user_id = #{userId}")
    List<Image> listByUserId(Long userId);

    // 按用户id批量删除（XML版）
    void deleteAllByUserId(Long userId);

    @Update("update images set alias = #{alias} where id = #{id}")
    void updateAlias(Long id,String alias);

    @Select("select * from images where id = #{id}")
    Image getById(Long id);

    List<Image> searchByAlias(String alias,Long userId);

    Integer countByUrl(String url);

}
