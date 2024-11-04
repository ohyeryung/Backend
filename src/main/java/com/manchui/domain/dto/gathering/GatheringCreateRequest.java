package com.manchui.domain.dto.gathering;

import com.manchui.domain.entity.Gathering;
import com.manchui.domain.entity.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GatheringCreateRequest {

    @NotNull(message = "이미지는 필수 입력값입니다.")
    private MultipartFile gatheringImage;

    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String category;

    @NotBlank(message = "모임명은 필수 입력값입니다.")
    @Size(min = 3, max = 30)
    private String groupName;

    @NotNull(message = "모임일자는 필수 입력값입니다.")
    private String gatheringDate;

    @NotBlank(message = "모임 장소는 필수 입력값입니다.")
    private String location;

    @NotNull(message = "모집 정원은 필수 입력값입니다.")
    @Min(2)
    @Max(100)
    private int maxUsers;

    @NotNull(message = "모집 최소 인원은 필수 입력값입니다.")
    @Min(2)
    @Max(100)
    private int minUsers;

    @Size(min = 10, max = 255)
    private String gatheringContent;

    public Gathering toRegisterEntity(User user, LocalDateTime gatheringDate, LocalDateTime dueDate) {

        return Gathering.builder()
                .user(user)
                .category(category)
                .groupName(groupName)
                .gatheringDate(gatheringDate)
                .dueDate(dueDate)
                .location(location)
                .maxUsers(maxUsers)
                .minUsers(minUsers)
                .gatheringContent(gatheringContent)
                .isHearted(false)
                .isOpened(false)
                .isCanceled(false)
                .isClosed(false)
                .build();
    }

}
