package com.sbeam.gameserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "players")
@EntityListeners(AuditingEntityListener.class)
public class Player {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "player_id", nullable = false)
        private Long id;

        @Size(max = 255)
        @Column(name = "email")
        private String email;

        @Size(max = 255)
        @NotNull
        @Column(name = "password_hash", nullable = false)
        private String passwordHash;

        @Size(max = 32)
        @NotNull
        @Column(name = "nickname", nullable = false, length = 32)
        private String nickname;

        @ColumnDefault("0")
        @Column(name = "status")
        private Byte status;

        @CreatedDate // 自动填充创建时间
        @Column(name = "created_at")
        private Instant createdAt;

        @LastModifiedDate // 自动填充更新时间
        @Column(name = "updated_at")
        private Instant updatedAt;

        @Column(name = "birthday")
        private Instant birthday;




}
