package com.manchui.domain.service;

import com.manchui.domain.dto.JoinDTO;
import com.manchui.domain.dto.NameDTO;
import com.manchui.domain.entity.User;
import com.manchui.domain.repository.UserRepository;
import com.manchui.global.exception.CustomException;
import com.manchui.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JoinService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(JoinDTO joinDTO) {

        String name = joinDTO.getName();
        String password = joinDTO.getPassword();
        String email = joinDTO.getEmail();
        String passwordConfirm = joinDTO.getPasswordConfirm();

        checkName(new NameDTO(name));

        //이메일 중복 검증
        if (isValidEmail(email)) {
            throw new CustomException(ErrorCode.ILLEGAL_EMAIL_DUPLICATION);
        }

        if(!isValidPassword(password)){
            throw new CustomException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }

        if (!password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String encodedPassword = bCryptPasswordEncoder.encode(password);
        User user = new User(name, email, encodedPassword);

        userRepository.save(user);
    }

    private boolean isValidEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private boolean isValidPassword(String password) {
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");

        int count = 0;
        if (hasLetter) count++;
        if (hasDigit) count++;
        if (hasSpecialChar) count++;

        return count >= 2;
    }

    public void checkName(NameDTO nameDTO) {

        Boolean isExist = userRepository.existsByName(nameDTO.getName());
        if (isExist) {
            throw new CustomException(ErrorCode.ILLEGAL_USERNAME_DUPLICATION);
        }
    }
}
