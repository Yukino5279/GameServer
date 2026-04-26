package com.sbeam.gameserver.service;

import com.sbeam.gameserver.entity.Player;
import com.sbeam.gameserver.exception.BusinessException;
import com.sbeam.gameserver.pojo.DTO.request.*;
import com.sbeam.gameserver.pojo.DTO.response.PlayerResponseDTO;
import com.sbeam.gameserver.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sbeam.gameserver.pojo.DTO.response.PlayerLoginResponseDTO;
import java.time.Instant;

@Service
public class PlayerAuthService {
    //声明构造函数
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JwtService jwtService;
    private final JwtTokenStoreService jwtTokenStoreService;

    public PlayerAuthService(PlayerRepository playerRepository,
                             PasswordEncoder passwordEncoder,
                             EmailVerificationService emailVerificationService,
                             JwtService jwtService,
                             JwtTokenStoreService jwtTokenStoreService) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.jwtService = jwtService;
        this.jwtTokenStoreService = jwtTokenStoreService;
    }

    //注册账号的校验邮箱
    public void sendRegisterVerificationCode(String email) {
        if (playerRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已被注册");
        }
        emailVerificationService.sendCode(email);
    }

    //创建完账号后的的校验邮箱
    public void sendVerificationCode(String email) {
        if (!playerRepository.existsByEmail(email)) {
            throw new BusinessException("账号不存在");
        }
        emailVerificationService.sendCode(email);
    }

    // 开启事务，确保数据库操作的原子性（要么全成功，要么全回滚）
    @Transactional
    public PlayerResponseDTO register(PlayerRegisterRequest request) {
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        emailVerificationService.verifyCodeOrThrow(request.getEmail(), request.getVerificationCode());

        // 创建新玩家实体并设置属性
        Player player = new Player();
        player.setEmail(request.getEmail());
        player.setNickname(request.getNickname());
        // 对密码进行加密存储，不保存明文密码
        player.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // 设置玩家状态为0（正常状态）
        player.setStatus((byte) 0);
        // 保存到数据库并返回响应DTO
        Player savedPlayer = playerRepository.save(player);
        return new PlayerResponseDTO(savedPlayer.getId(), savedPlayer.getEmail(), savedPlayer.getNickname());
    }

    // 用户登录
    @Transactional
    public PlayerLoginResponseDTO login(PlayerLoginRequest request) {
        Player player = playerRepository.findByEmail(request.getEmail()) //理解成else，当为空的时候才执行->。这样做可以节省内存空间
                .orElseThrow(() -> new BusinessException("账号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        return issueTokenPair(player);
    }

    //Token刷新
    @Transactional
    public PlayerLoginResponseDTO refreshToken(String refreshToken) {
        String email;
        try {
            email = jwtService.extractEmail(refreshToken);
        } catch (Exception e) {
            throw new BusinessException("refreshToken 非法或已过期");
        }
        Player player = playerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("账号不存在"));

        String redisRefreshToken = jwtTokenStoreService.getRefreshToken(email);
        if (redisRefreshToken == null
                || !redisRefreshToken.equals(refreshToken)
                || !jwtService.isRefreshTokenValid(refreshToken, email)) {
            throw new BusinessException("refreshToken 无效，请重新登录");
        }
        return issueTokenPair(player);
    }

    //登出账号
    @Transactional
    public void logout(String email) {
        Player player = playerRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("账号不存在"));

        jwtTokenStoreService.deleteAllTokens(player.getEmail());
    }


    //注销账号
    @Transactional
    public void deleteAccount(PlayerDeleteAccountRequest request) {
        Player player = playerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("账号不存在"));
        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }
        emailVerificationService.verifyCodeOrThrow(request.getEmail(), request.getVerificationCode());
        playerRepository.delete(player);
        jwtTokenStoreService.deleteAllTokens(player.getEmail());
    }



    //更新昵称
    @Transactional
    public PlayerResponseDTO updateNickname(PlayerNicknameUpdateRequest request) {
        Player player = playerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("账号不存在"));

        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }

        player.setNickname(request.getNewNickname());
        Player updatedPlayer = playerRepository.save(player);
        return new PlayerResponseDTO(updatedPlayer.getId(), updatedPlayer.getEmail(), updatedPlayer.getNickname());
    }

    //更新密码
    @Transactional
    public void updatePassword(PlayerPasswordUpdateRequest request) {
        Player player = playerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("账号不存在"));

        if (!passwordEncoder.matches(request.getOldPassword(), player.getPasswordHash())) {
            throw new BusinessException("原密码错误");
        }

        emailVerificationService.verifyCodeOrThrow(request.getEmail(), request.getVerificationCode());
        player.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        playerRepository.save(player);
        jwtTokenStoreService.deleteAllTokens(player.getEmail());
    }

    private PlayerLoginResponseDTO issueTokenPair(Player player) {
        //生成
        String accessToken = jwtService.generateAccessToken(player.getEmail());
        String refreshToken = jwtService.generateRefreshToken(player.getEmail());
        //解析
        Instant accessExpireAt = jwtService.extractExpiration(accessToken);
        Instant refreshExpireAt = jwtService.extractExpiration(refreshToken);
        //存入redis
        jwtTokenStoreService.saveAccessToken(player.getEmail(), accessToken, jwtService.getAccessTokenTtl());
        jwtTokenStoreService.saveRefreshToken(player.getEmail(), refreshToken, jwtService.getRefreshTokenTtl());
        //返回前端
        PlayerResponseDTO playerResponseDTO = new PlayerResponseDTO(player.getId(), player.getEmail(), player.getNickname());
        return new PlayerLoginResponseDTO(playerResponseDTO, accessToken, accessExpireAt, refreshToken, refreshExpireAt);
    }
}