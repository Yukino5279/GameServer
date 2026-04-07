package com.sbeam.gameserver.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "players")
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

        @ColumnDefault("CURRENT_TIMESTAMP")
        @Column(name = "created_at")
        private Instant createdAt;

        @ColumnDefault("CURRENT_TIMESTAMP")
        @Column(name = "birthday")
        private Instant birthday;

        @ColumnDefault("CURRENT_TIMESTAMP")
        @Column(name = "updated_at")
        private Instant updatedAt;


}
