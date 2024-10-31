package com.manchui.domain.service;

import com.manchui.domain.dto.User.UserEditInfoRequest;
import com.manchui.domain.dto.User.UserInfoResponse;
import com.manchui.domain.entity.Image;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.ImageRepository;
import com.manchui.domain.repository.UserRepository;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.manchui.global.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageServiceImpl imageService;
    private final ImageRepository imageRepository;

    // 유저 객체 검증
    public User checkUser(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return user;
    }

    //유저 정보 반환
    public UserInfoResponse getUserInfo(String userEmail) {
        User user = userRepository.findByEmail(userEmail);

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .image(user.getProfileImagePath())
                .build();
    }

    //유저 정보 수정
    @Transactional
    public UUID editUserInfo(String userEmail, UserEditInfoRequest userEditInfoRequest) {

        User user = userRepository.findByEmail(userEmail);
        MultipartFile image = userEditInfoRequest.getImage();
        String name = userEditInfoRequest.getName();
        Long imageId = imageService.uploadUserProfileImage(image);
        user.editName(name);
        Image findImage = imageRepository.findById(imageId)
                .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
        String filePath = findImage.getFilePath();
        user.editProfileImagePath(filePath);
        return user.getId();
    }

    //유저 조회
    public User findByUserId(UUID userId) {

        return userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    //유저 이름 중복 확인
    public void checkName(String name) {
        if (userRepository.existsByName(name)) {
            throw new CustomException(ILLEGAL_USERNAME_DUPLICATION);
        }
    }
}
