package me.ezzi.myimagehostbackend.common.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.List;

@AllArgsConstructor
@Data
@Slf4j
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    private String customDomain; // 自定义域名

    /**
     * 文件上传
     */
    public String upload(byte[] bytes, String objectName) {

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(bytes));
        } catch (OSSException oe) {
            log.error("OSS 上传失败：{}", oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：{}", ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }

        // 使用自定义域名，如果没有配置则使用默认OSS域名
        String url;
        if (customDomain != null && !customDomain.isEmpty()) {
            url = "https://" + customDomain + "/" + objectName;
        } else {
            url = "https://" + bucketName + "." + endpoint + "/" + objectName;
        }
        log.info("文件上传成功: {}", url);
        return url;
    }

    /**
     * 删除 OSS 中的文件
     *
     * @param objectName OSS 文件路径，例如：folder/test.png
     */
    public void delete(String objectName) {

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            ossClient.deleteObject(bucketName, objectName);
            log.info("文件已删除：{}", objectName);
        } catch (OSSException oe) {
            log.error("OSS 删除失败：{}", oe.getErrorMessage());
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：{}", ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public void deleteBatch(List<String> objectNames) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            for (String objectName : objectNames) {
                try {
                    ossClient.deleteObject(bucketName, objectName);
                    log.info("已删除：{}", objectName);
                } catch (OSSException oe) {
                    log.error("删除失败 [{}]：{}", objectName, oe.getErrorMessage());
                }
            }
            log.info("批量删除完成，共 {} 个文件", objectNames.size());
        } catch (ClientException ce) {
            log.error("OSS 客户端异常：{}", ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }



}
