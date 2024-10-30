package com.manchui.domain.entity;

import com.manchui.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "attendance")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Attendance extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("참석 id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "gathering_id", nullable = false)
    @Comment("모임 id")
    private Gathering gathering;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("회원 id")
    private User user;

}
