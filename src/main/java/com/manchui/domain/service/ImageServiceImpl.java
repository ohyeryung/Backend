package com.manchui.domain.service;

import com.manchui.domain.entity.Image;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl {

    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;

    private static final Set<String> VALID_EXTENSIONS = new HashSet<>(Arrays.asList("gif", "png", "jpg", "jpeg",
            "GIF", "PNG", "JPG", "JPEG"));

    // 모임의 Image 등록
    @Transactional
    public void uploadGatheringImage(MultipartFile multipartFile, Long gatheringId) {
        imageRepository.save(toImageEntity(multipartFile, gatheringId));

    }

    // MultipartFile 을 Image Entity 형태로 변경
    public Image toImageEntity(MultipartFile multipartFile, Long gatheringId) {

        if(multipartFile.isEmpty()) throw new CustomException(ErrorCode.ILLEGAL_EMPTY_FILE);

        String fakeFileName = createRandomFileName(multipartFile.getOriginalFilename());
        String originalFileName = multipartFile.getOriginalFilename();
        String filePath = s3Uploader.uploadImage(multipartFile, fakeFileName);

        return new Image(originalFileName, fakeFileName,  filePath, gatheringId);

    }

    // 파일명 난수화
    public String createRandomFileName(String fileName) {

        return UUID.randomUUID().toString().concat(getFileExtension(fileName));

    }

    // 파일 확장명 체크
    public String getFileExtension(String fileName) {

        try {
            int idx = fileName.lastIndexOf(".");
            if (idx == -1 || idx == fileName.length() - 1) {
                throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
            }

            String extension = fileName.substring(idx + 1);

            if (!VALID_EXTENSIONS.contains(extension)) {
                throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
            }
            return "." + extension;
        }
        catch (IndexOutOfBoundsException e) {
            throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
        }
    }
}
