package com.sbeam.gameserver.controller;

import com.sbeam.gameserver.pojo.DTO.request.*;
import com.sbeam.gameserver.pojo.DTO.response.AuthResponseDTO;
import com.sbeam.gameserver.pojo.DTO.response.MessageResponseDTO;
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

    @PostMapping("/register/verification-code")
    public MessageResponseDTO sendRegisterVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
        playerAuthService.sendRegisterVerificationCode(request.getEmail());
        return new MessageResponseDTO("验证码发送成功");
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody PlayerLoginRequest request) {
        PlayerResponseDTO player = playerAuthService.login(request);
        return new AuthResponseDTO("登录成功", player);
    }
    @PostMapping("/logout")
    public MessageResponseDTO logout(@Valid @RequestBody PlayerLogoutRequest request) {
        playerAuthService.logout(request);
        return new MessageResponseDTO("退出登录成功，已清除该设备登录信息");
    }

    @PostMapping("/delete-account")
    public MessageResponseDTO deleteAccount(@Valid @RequestBody PlayerDeleteAccountRequest request) {
        playerAuthService.deleteAccount(request);
        return new MessageResponseDTO("账号注销成功");
    }

    @PostMapping("/nickname")
    public AuthResponseDTO updateNickname(@Valid @RequestBody PlayerNicknameUpdateRequest request) {
        PlayerResponseDTO player = playerAuthService.updateNickname(request);
        return new AuthResponseDTO("用户名修改成功", player);
    }

    @PostMapping("/verification-code")
    public MessageResponseDTO sendVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
        playerAuthService.sendVerificationCode(request.getEmail());
        return new MessageResponseDTO("验证码发送成功");
    }

    @PostMapping("/password")
    public MessageResponseDTO updatePassword(@Valid @RequestBody PlayerPasswordUpdateRequest request) {
        playerAuthService.updatePassword(request);
        return new MessageResponseDTO("密码修改成功");
    }
}