package com.manchui.domain.entity;

import com.manchui.global.entity.Timestamped;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String oauth2Id;
    private String name;
    private String email;
    private String password;
    private String profileImagePath;

    public User(String email) {
        this.email = email;
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public User(String oAuth2Id, String email) {
        this.oauth2Id = oAuth2Id;
        this.email = email;
    }

    public void editEmail(String email) {
        this.email = email;
    }

    public void editName(String name) {
        this.name = name;
    }

    public void editProfileImagePath(String filePath) {
        this.profileImagePath = filePath;
    }
}
