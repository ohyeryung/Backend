package com.manchui.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "image")
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    @Comment("이미지 id")
    private Long imageId;

    @Column(name = "original_file_name")
    @Comment("원본 파일명")
    private String originalFileName;

    @Column(name = "fake_file_name", unique = true)
    @Comment("생성된 파일명")
    private String fakeFileName;

    @Column(name = "file_path")
    @Comment("파일 경로")
    private String filePath;

    @Column(name = "gathering_id")
    @Comment("모임 id")
    private Long gatheringId;


    public Image(String originalFileName, String fakeFileName, String filePath, Long gatheringId) {

        this.originalFileName = originalFileName;
        this.fakeFileName = fakeFileName;
        this.filePath = filePath;
        this.gatheringId = gatheringId;

    }

    public Image(String originalFileName, String fakeFileName, String filePath) {
        this.originalFileName = originalFileName;
        this.fakeFileName = fakeFileName;
        this.filePath = filePath;

    }

}
