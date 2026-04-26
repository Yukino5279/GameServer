package com.sbeam.gameserver.controller;

import com.sbeam.gameserver.pojo.DTO.request.*;
import com.sbeam.gameserver.pojo.DTO.response.AuthResponseDTO;
import com.sbeam.gameserver.pojo.DTO.response.MessageResponseDTO;
import com.sbeam.gameserver.pojo.DTO.response.PlayerResponseDTO;
import com.sbeam.gameserver.service.PlayerAuthService;
import com.sbeam.gameserver.pojo.DTO.response.PlayerLoginResponseDTO;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

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
        return new AuthResponseDTO("注册成功", player, null, null, null, null);
    }

    @PostMapping("/register/verification-code")
    public MessageResponseDTO sendRegisterVerificationCode(@Valid @RequestBody EmailVerificationCodeRequest request) {
        playerAuthService.sendRegisterVerificationCode(request.getEmail());
        return new MessageResponseDTO("验证码发送成功");
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody PlayerLoginRequest request) {
        PlayerLoginResponseDTO loginResult = playerAuthService.login(request);
        return new AuthResponseDTO(
                "登录成功",
                loginResult.player(),
                loginResult.accessToken(),
                loginResult.accessTokenExpireAt(),
                loginResult.refreshToken(),
                loginResult.refreshTokenExpireAt()
        );
    }

    @PostMapping("/refresh-token")
    public AuthResponseDTO refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        PlayerLoginResponseDTO refreshResult = playerAuthService.refreshToken(request.getRefreshToken());
        return new AuthResponseDTO(
                "Token 刷新成功",
                refreshResult.player(),
                refreshResult.accessToken(),
                refreshResult.accessTokenExpireAt(),
                refreshResult.refreshToken(),
                refreshResult.refreshTokenExpireAt()
        );
    }


    @PostMapping("/logout")
    public MessageResponseDTO logout(Authentication authentication) {
        playerAuthService.logout(authentication.getName());
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
        return new AuthResponseDTO("用户名修改成功", player, null, null, null, null);
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