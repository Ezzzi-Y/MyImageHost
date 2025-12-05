package me.ezzi.myimagehostbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import me.ezzi.myimagehostbackend.common.constant.MessageConstant;
import me.ezzi.myimagehostbackend.common.constant.RedisConstant;
import me.ezzi.myimagehostbackend.common.utils.AliOssUtil;
import me.ezzi.myimagehostbackend.exception.BaseException;
import me.ezzi.myimagehostbackend.mapper.ImageMapper;
import me.ezzi.myimagehostbackend.pojo.dto.ImageDTO;
import me.ezzi.myimagehostbackend.pojo.dto.UpdateAliasDTO;
import me.ezzi.myimagehostbackend.pojo.entity.Image;
import me.ezzi.myimagehostbackend.pojo.entity.PageResult;
import me.ezzi.myimagehostbackend.pojo.vo.UserVO;
import me.ezzi.myimagehostbackend.properties.AliOssProperties;
import me.ezzi.myimagehostbackend.service.ImageService;
import me.ezzi.myimagehostbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageMapper imageMapper;
    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private AliOssProperties aliOssProperties;

    private static final Set<String> ALLOW_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadFile(ImageDTO imageDTO) {
        MultipartFile file = imageDTO.getFile();
        Assert.notNull(file, MessageConstant.LACK_PARAM);
        
        String originalFileName = file.getOriginalFilename();
        Assert.hasText(originalFileName, MessageConstant.LACK_PARAM);

        String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOW_TYPES.contains(extension)) {
            throw new BaseException(MessageConstant.UNSAFE_FILE_TYPE);
        }

        String md5;
        try {
            md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        } catch (IOException e) {
            throw new BaseException(MessageConstant.FILE_READ_ERROR);
        }

        String redisKey = RedisConstant.FILE_MD5_PREFIX + md5;
        String existUrl = (String) redisTemplate.opsForValue().get(redisKey);

        Long userId = StpUtil.getLoginIdAsLong();
        UserVO userVO = userService.getUserById(userId);
        long size = file.getSize();

        if (userVO.getUsedCount() >= userVO.getQuotaCount()) {
            throw new BaseException(MessageConstant.COUNT_FULL);
        }
        if (userVO.getUsedSpace() + size > userVO.getQuotaSpace()) {
            throw new BaseException(MessageConstant.SPACE_FULL);
        }

        String url;
        if (existUrl != null && !existUrl.isEmpty()) {
            url = existUrl;
            log.info("文件秒传成功: {}", url);
        } else {
            LocalDate date = LocalDate.now();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String objectName = date.getYear() + "/" + date.getMonthValue() + "/" + uuid + "." + extension;
            try {
                url = aliOssUtil.upload(file.getBytes(), objectName);
                redisTemplate.opsForValue().set(redisKey, url);
            } catch (IOException e) {
                throw new BaseException(MessageConstant.UPLOAD_FAILED);
            }
        }

        Image image = Image.builder()
                .userId(userId)
                .originalName(originalFileName)
                .alias(imageDTO.getAlias())
                .url(url)
                .md5(md5)
                .size(size)
                .createTime(LocalDateTime.now())
                .build();

        imageMapper.insert(image);
        userService.upload(size);
    }

    @Override
    public PageResult listImage(Integer page, Integer pageSize) {
        PageHelper.startPage(page, pageSize);
        Long userId = StpUtil.getLoginIdAsLong();
        Page<Image> pageList = imageMapper.pageList(userId);
        return new PageResult(pageList.getTotal(), pageList.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        
        Long userId = StpUtil.getLoginIdAsLong();
        List<Image> list = imageMapper.queryBatch(ids);
        
        if (list == null || list.isEmpty()) return;

        boolean hasIllegal = list.stream().anyMatch(img -> !img.getUserId().equals(userId));
        if (hasIllegal) {
            throw new BaseException(MessageConstant.USER_WRONG);
        }

        // 计算需要扣减的空间和数量
        long totalSize = list.stream().mapToLong(Image::getSize).sum();
        int count = list.size();

        // 在删除数据库记录之前，先检查哪些URL只被当前要删除的记录引用
        // 只有当URL的引用计数等于当前批次中该URL出现的次数时，才删除OSS文件
        List<String> objectNamesToDelete = list.stream()
                .map(Image::getUrl)
                .distinct()
                .filter(url -> {
                    long totalCount = imageMapper.countByUrl(url);
                    long deleteCount = list.stream().filter(img -> img.getUrl().equals(url)).count();
                    return totalCount == deleteCount;
                })
                .map(this::extractObjectName)
                .collect(Collectors.toList());

        // 收集需要删除的MD5对应的Redis key（只删除最后一个引用）
        List<String> redisKeysToDelete = list.stream()
                .filter(img -> {
                    long totalCount = imageMapper.countByUrl(img.getUrl());
                    long deleteCount = list.stream().filter(i -> i.getUrl().equals(img.getUrl())).count();
                    return totalCount == deleteCount;
                })
                .map(img -> RedisConstant.FILE_MD5_PREFIX + img.getMd5())
                .distinct()
                .collect(Collectors.toList());

        // 删除数据库记录
        imageMapper.deleteBatch(ids);

        // 删除OSS文件（只删除不再被引用的文件）
        if (!objectNamesToDelete.isEmpty()) {
            aliOssUtil.deleteBatch(objectNamesToDelete);
        }

        // 删除Redis缓存（只删除不再被引用的MD5记录）
        if (!redisKeysToDelete.isEmpty()) {
            redisTemplate.delete(redisKeysToDelete);
        }

        // 扣减用户配额
        userService.delete(totalSize, count);
    }

    @Override
    public void updateAlias(UpdateAliasDTO updateAliasDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        Image image = imageMapper.getById(updateAliasDTO.getId());
        Assert.notNull(image, MessageConstant.UNKNOWN_ERROR);
        
        if (!image.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.USER_WRONG);
        }
        imageMapper.updateAlias(updateAliasDTO.getId(), updateAliasDTO.getAlias());
    }

    @Override
    public List<Image> searchImageByAlias(String alias) {
        Long userId = StpUtil.getLoginIdAsLong();
        return imageMapper.searchByAlias(alias, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUserId(Long userId) {
        List<Image> images = imageMapper.listByUserId(userId);
        if (!images.isEmpty()) {
            List<String> objectNames = images.stream()
                    .map(img -> extractObjectName(img.getUrl()))
                    .collect(Collectors.toList());
            aliOssUtil.deleteBatch(objectNames);
        }
        imageMapper.deleteByUserId(userId);
    }

    private String extractObjectName(String url) {
        String host = "https://" + aliOssProperties.getBucketName() + "." + aliOssProperties.getEndpoint() + "/";
        if (url.startsWith(host)) {
            return url.substring(host.length());
        }
        int index = url.indexOf(".com/");
        if (index != -1) {
            return url.substring(index + 5);
        }
        throw new BaseException("URL 格式错误，无法提取 objectName: " + url);
    }
}
