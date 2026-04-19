package com.sbeam.gameserver.service;

import com.sbeam.gameserver.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmailService emailService;
    private final StringRedisTemplate stringRedisTemplate;

    public EmailVerificationService(EmailService emailService,
                                    StringRedisTemplate stringRedisTemplate) {
        this.emailService = emailService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendCode(String email) {
        String resendCooldownKey = buildResendCooldownKey(email);
        Boolean canSend = stringRedisTemplate.opsForValue()
                .setIfAbsent(resendCooldownKey, "1", RESEND_INTERVAL);
        if (Boolean.FALSE.equals(canSend)) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        stringRedisTemplate.opsForValue().set(buildCodeKey(email), code, CODE_TTL);
        emailService.sendVerificationCode(email, code);
    }

    public void verifyCodeOrThrow(String email, String code) {
        String redisCode = stringRedisTemplate.opsForValue().get(buildCodeKey(email));
        if (redisCode == null) {
            throw new BusinessException("请先获取邮箱验证码");
        }
        if (!redisCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }
        stringRedisTemplate.delete(buildCodeKey(email));
    }
    private String buildCodeKey(String email) {
        return "email:verification:code:" + email;
    }


    private String buildResendCooldownKey(String email) {
        return "email:verification:resend:" + email;
    }
}