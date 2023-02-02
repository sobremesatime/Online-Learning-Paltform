package com.xuecheng.media;

import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Bucket;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @Title: MinIoTest
 * @Description: 测试minio的上传，删除，查询
 * @author: LYL
 * @date: 2023/1/31 20:50
 */
public class MinIoTest {
    static MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://175.178.70.89:9000")
                    .credentials("miniousername", "miniopassword")
                    .build();

    //上传文件
    @Test
    public void upload() {

        try {
            System.out.println(minioClient.bucketExists(BucketExistsArgs.builder().bucket("testbucket").build()));

        } catch (Exception e) {
            System.out.println("连接异常");
        }
        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("sign.jpg")//同一个桶内对象名不能重复
                    .filename("C:\\Users\\long\\Pictures\\Camera Roll4\\sign.jpg")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败");
        }
    }
    //指定桶内的子目录
    @Test
    public void upload2() {

        try {
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")
                    .object("test/1.avi")//同一个桶内对象名不能重复
                    .filename("D:\\develop\\upload\\1.avi")
                    .build();
            //上传
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传成功了");
        } catch (Exception e) {
            System.out.println("上传失败");
        }


    }
    //删除文件
    @Test
    public void delete() {
        try {
            RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("test/1.avi").build();
            minioClient.removeObject(removeObjectArgs);
        } catch (Exception e) {
        }

    }
    //查询文件
    @Test
    public void getFile() {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("1.mp4").build();
        try(
                FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
                FileOutputStream outputStream = new FileOutputStream(new File("D:\\develop\\upload\\1_1.mp4"));
        ) {

            if(inputStream!=null){
                IOUtils.copy(inputStream,outputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
