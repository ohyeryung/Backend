package com.manchui.domain.service;

import com.manchui.domain.entity.User;
import com.manchui.domain.repository.UserRepository;
import com.manchui.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.manchui.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 유저 객체 검증
    public User checkUser(String email) {

        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return user;
    }

}
