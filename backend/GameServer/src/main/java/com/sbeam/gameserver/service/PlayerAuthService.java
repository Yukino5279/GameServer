package com.sbeam.gameserver.service;

import com.sbeam.gameserver.entity.Player;
import com.sbeam.gameserver.exception.BusinessException;
import com.sbeam.gameserver.pojo.DTO.request.PlayerLoginRequest;
import com.sbeam.gameserver.pojo.DTO.request.PlayerRegisterRequest;
import com.sbeam.gameserver.pojo.DTO.response.PlayerResponseDTO;
import com.sbeam.gameserver.repository.PlayerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerAuthService {
    //声明构造函数
    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerAuthService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 开启事务，确保数据库操作的原子性（要么全成功，要么全回滚）
    @Transactional
    public PlayerResponseDTO register(PlayerRegisterRequest request) {
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        if (playerRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException("昵称已被占用");
        }
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

    // 设置只读，优化查询时间
    @Transactional(readOnly = true)
    public PlayerResponseDTO login(PlayerLoginRequest request) {
        //理解成else，当为空的时候才执行->。这样做可以节省内存空间
        Player player = playerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("账号或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new BusinessException("账号或密码错误");
        }

        return new PlayerResponseDTO(player.getId(), player.getEmail(), player.getNickname());
    }
}