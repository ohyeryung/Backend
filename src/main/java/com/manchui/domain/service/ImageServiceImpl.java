package com.manchui.domain.service;

import com.manchui.domain.entity.Image;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.mock.web.MockMultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl {

    private static final Set<String> VALID_EXTENSIONS = new HashSet<>(Arrays.asList("gif", "png", "jpg", "jpeg",
            "GIF", "PNG", "JPG", "JPEG"));
    private final ImageRepository imageRepository;
    private final S3Uploader s3Uploader;

    // 모임의 Image 등록
    @Transactional
    public void uploadGatheringImage(MultipartFile multipartFile, Long gatheringId, boolean isDuplicate) {

        if (isDuplicate) {
            Image foundImage = imageRepository.findByGatheringId(gatheringId);
            log.info("기존 이미지 이름 :  {}", foundImage.getOriginalFileName());

            s3Uploader.deleteImage(foundImage.getFakeFileName());
            imageRepository.delete(foundImage);
        }
        imageRepository.save(toImageEntity(multipartFile, gatheringId));

    }

    public Long uploadUserProfileImage(MultipartFile multipartFile) {

        return imageRepository.save(toImageEntity(multipartFile, null)).getImageId();
    }


    // MultipartFile 을 Image Entity 형태로 변경
    public Image toImageEntity(MultipartFile multipartFile, Long gatheringId) {

        if (multipartFile.isEmpty()) throw new CustomException(ErrorCode.ILLEGAL_EMPTY_FILE);

        String fakeFileName = createRandomFileName(multipartFile.getOriginalFilename());
        String originalFileName = multipartFile.getOriginalFilename();
        String filePath = s3Uploader.uploadImage(multipartFile, fakeFileName);

        if (gatheringId == null) {
            return new Image(originalFileName, fakeFileName, filePath);
        }

        return new Image(originalFileName, fakeFileName, filePath, gatheringId);

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
        } catch (IndexOutOfBoundsException e) {
            throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
        }
    }

    // URL로 받은 프로필 이미지를 저장
    public Long uploadUserProfileImageFromUrl(String imageUrl) {
        try {
            // URL에서 이미지 다운로드
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new CustomException(ErrorCode.ILLEGAL_IMAGE_URL);
            }
            String contentType = connection.getContentType();
            if (!isValidContentType(contentType)) {
                throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
            }
            String fileExtension = getFileExtensionFromContentType(contentType);
            try (InputStream inputStream = connection.getInputStream()) {
                // 이미지 데이터를 MockMultipartFile로 변환
                MockMultipartFile multipartFile = new MockMultipartFile(
                        "file", // 파일 이름
                        "profile" + fileExtension, // 파일명
                        contentType, // Content-Type
                        inputStream // 파일 데이터
                );
                // 기존 uploadUserProfileImage 메서드 재사용
                return uploadUserProfileImage(multipartFile);
            }
        } catch (Exception e) {
            log.error("URL에서 프로필 이미지를 업로드하는 데 실패했습니다.: {}", imageUrl, e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // Content-Type 검증
    private boolean isValidContentType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    // Content-Type으로부터 파일 확장자 추출
    private String getFileExtensionFromContentType(String contentType) {
        switch (contentType) {
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            default:
                throw new CustomException(ErrorCode.WRONG_TYPE_IMAGE);
        }
    }

}
