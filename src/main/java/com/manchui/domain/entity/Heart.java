package com.manchui.domain.entity;

import com.manchui.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "heart")
public class Heart extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "gathering_id", nullable = false)
    @Comment("모임 id")
    private Gathering gathering;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @Comment("회원 id")
    private User user;

}
