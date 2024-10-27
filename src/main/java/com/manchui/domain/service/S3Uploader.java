package com.manchui.domain.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.manchui.global.exception.ErrorCode.FAILED_UPLOAD_IMAGE;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private  final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private  String bucket;

    // S3 이미지 업로드
    public String uploadImage(MultipartFile file, String fakeFileName){
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, fakeFileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new CustomException(FAILED_UPLOAD_IMAGE);
        }

        return amazonS3.getUrl(bucket, fakeFileName).toString();
    }

    // S3 이미지 삭제
    public void deleteImage(String fileName){
        amazonS3.deleteObject(bucket, fileName);
    }

}
