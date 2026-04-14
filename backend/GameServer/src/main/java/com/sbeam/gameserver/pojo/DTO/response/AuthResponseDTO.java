package com.sbeam.gameserver.pojo.DTO.response;

public record AuthResponseDTO(
        String message,
        PlayerResponseDTO player
) {
}