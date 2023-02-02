package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    MinioClient minioClient;

    @Value("${minio.bucket.files}")
    private String bucket_files;

    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto) {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
        //得到文件的md5值
        String fileMd5 = DigestUtils.md5Hex(bytes);

        if(StringUtils.isEmpty(folder)){
            //自动生成目录的路径 按年月日生成，
            folder = getFileFolder(new Date(), true, true, true);
        }else if(folder.indexOf("/")<0){
            folder = folder+"/";
        }
        //文件名称
        String filename = uploadFileParamsDto.getFilename();

        if(StringUtils.isEmpty(objectName)){
            //如果objectName为空，使用文件的md5值为objectName
            objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
        }

        objectName = folder + objectName;

        try {
            addMediaFilesToMinIO(bytes, bucket_files, objectName);
            MediaFiles mediaFiles = addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_files, objectName);
//            //保存到数据库
//            MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
//            if(mediaFiles == null){
//                mediaFiles = new MediaFiles();
//
//                //封装数据
//                BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
//                mediaFiles.setId(fileMd5);
//                mediaFiles.setFileId(fileMd5);
//                mediaFiles.setCompanyId(companyId);
//                mediaFiles.setFilename(filename);
//                mediaFiles.setBucket(bucket_files);
//                mediaFiles.setFilePath(objectName);
//                mediaFiles.setUrl("/"+bucket_files+"/"+objectName);
//                mediaFiles.setCreateDate(LocalDateTime.now());
//                mediaFiles.setStatus("1");
//                mediaFiles.setAuditStatus("002003");
//                //插入文件表
//                mediaFilesMapper.insert(mediaFiles);
//            }

            //准备返回数据
            UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
            BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
            return uploadFileResultDto;


        } catch (Exception e) {
            log.debug("上传文件失败：{}",e.getMessage());
        }

        return null;
    }

    //将文件上传到分布式文件系统
    private void addMediaFilesToMinIO(byte[] bytes, String bucket, String objectName) {

        //资源的媒体类型
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//默认未知二进制流

        if (objectName.indexOf(".") >= 0) {
            //取objectName中的扩展名
            String extension = objectName.substring(objectName.lastIndexOf("."));
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
            if (extensionMatch != null) {
                contentType = extensionMatch.getMimeType();
            }

        }

        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    //InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
                    .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
                    .contentType(contentType)
                    .build();
            //上传到minio
            minioClient.putObject(putObjectArgs);

        } catch (Exception e) {
            e.printStackTrace();
            log.debug("上传文件到文件系统出错:{}", e.getMessage());
            XueChengPlusException.cast("上传文件到文件系统出错");
        }
    }

    @Transactional
    //保存到数据库
    public MediaFiles addMediaFilesToDb(Long companyId, String fileId, UploadFileParamsDto uploadFileParamsDto, String bucket, String objectName) {
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        if (mediaFiles == null) {
            mediaFiles = new MediaFiles();

            //封装数据
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(fileId);
            mediaFiles.setFileId(fileId);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            mediaFiles.setCreateDate(LocalDateTime.now());
            mediaFiles.setStatus("1");
            mediaFiles.setAuditStatus("002003");


            //插入文件表
            mediaFilesMapper.insert(mediaFiles);

            //抛出异常,制造异常
//            int i=1/0;
        }
        return mediaFiles;
    }

    //根据日期拼接目录
    private String getFileFolder(Date date, boolean year, boolean month, boolean day){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前日期字符串
        String dateString = sdf.format(new Date());
        //取出年、月、日
        String[] dateStringArray = dateString.split("-");
        StringBuffer folderString = new StringBuffer();
        if(year){
            folderString.append(dateStringArray[0]);
            folderString.append("/");
        }
        if(month){
            folderString.append(dateStringArray[1]);
            folderString.append("/");
        }
        if(day){
            folderString.append(dateStringArray[2]);
            folderString.append("/");
        }
        return folderString.toString();
    }
}

//原来的方法
//@Override
//public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, byte[] bytes, String folder, String objectName) {
//
//
//    //得到文件的md5值
//    String fileMd5 = DigestUtils.md5Hex(bytes);
//
//    if(StringUtils.isEmpty(folder)){
//        //自动生成目录的路径 按年月日生成，
//        folder = getFileFolder(new Date(), true, true, true);
//    }else if(folder.indexOf("/")<0){
//        folder = folder+"/";
//    }
//    //文件名称
//    String filename = uploadFileParamsDto.getFilename();
//
//    if(StringUtils.isEmpty(objectName)){
//        //如果objectName为空，使用文件的md5值为objectName
//        objectName = fileMd5 + filename.substring(filename.lastIndexOf("."));
//    }
//
//    objectName = folder + objectName;
//
//    try {
//        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
//        String contentType = uploadFileParamsDto.getContentType();
//
//        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                .bucket(bucket_files)
//                .object(objectName)
//                //InputStream stream, long objectSize 对象大小, long partSize 分片大小(-1表示5M,最大不要超过5T，最多10000)
//                .stream(byteArrayInputStream, byteArrayInputStream.available(), -1)
//                .contentType(contentType)
//                .build();
//        //上传到minio
//        minioClient.putObject(putObjectArgs);
//
//        //保存到数据库
//        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
//        if(mediaFiles == null){
//            mediaFiles = new MediaFiles();
//
//            //封装数据
//            BeanUtils.copyProperties(uploadFileParamsDto,mediaFiles);
//            mediaFiles.setId(fileMd5);
//            mediaFiles.setFileId(fileMd5);
//            mediaFiles.setCompanyId(companyId);
//            mediaFiles.setFilename(filename);
//            mediaFiles.setBucket(bucket_files);
//            mediaFiles.setFilePath(objectName);
//            mediaFiles.setUrl("/"+bucket_files+"/"+objectName);
//            mediaFiles.setCreateDate(LocalDateTime.now());
//            mediaFiles.setStatus("1");
//            mediaFiles.setAuditStatus("002003");
//
//            //插入文件表
//            mediaFilesMapper.insert(mediaFiles);
//
//        }
//
//        //准备返回数据
//        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
//        BeanUtils.copyProperties(mediaFiles,uploadFileResultDto);
//        return uploadFileResultDto;
//
//
//    } catch (Exception e) {
//        log.debug("上传文件失败：{}",e.getMessage());
//    }
//
//    return null;
//}

