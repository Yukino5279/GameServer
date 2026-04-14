package com.sbeam.gameserver.controller;

import com.sbeam.gameserver.pojo.DTO.request.PlayerLoginRequest;
import com.sbeam.gameserver.pojo.DTO.request.PlayerRegisterRequest;
import com.sbeam.gameserver.pojo.DTO.response.AuthResponseDTO;
import com.sbeam.gameserver.pojo.DTO.response.PlayerResponseDTO;
import com.sbeam.gameserver.service.PlayerAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
public class PlayerAuthController {

    private final PlayerAuthService playerAuthService;

    public PlayerAuthController(PlayerAuthService playerAuthService) {
        this.playerAuthService = playerAuthService;
    }

    @PostMapping("/register")
    public AuthResponseDTO register(@Valid @RequestBody PlayerRegisterRequest request) {
        PlayerResponseDTO player = playerAuthService.register(request);
        return new AuthResponseDTO("注册成功", player);
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody PlayerLoginRequest request) {
        PlayerResponseDTO player = playerAuthService.login(request);
        return new AuthResponseDTO("登录成功", player);
    }
}