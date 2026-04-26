package com.sbeam.gameserver.pojo.DTO.response;

import java.time.Instant;

public record PlayerLoginResponseDTO(
        PlayerResponseDTO player,
        String accessToken,
        Instant accessTokenExpireAt,
        String refreshToken,
        Instant refreshTokenExpireAt
) {
}