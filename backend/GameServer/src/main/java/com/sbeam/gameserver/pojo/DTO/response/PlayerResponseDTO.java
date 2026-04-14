package com.sbeam.gameserver.pojo.DTO.response;

public record PlayerResponseDTO(
        Long id,
        String email,
        String nickname
) {
}
