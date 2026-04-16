package com.sbeam.gameserver.service;

import com.sbeam.gameserver.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final Random RANDOM = new Random();

    private final ResendEmailService resendEmailService;
    private final Map<String, VerificationRecord> cache = new ConcurrentHashMap<>();

    public EmailVerificationService(ResendEmailService resendEmailService) {
        this.resendEmailService = resendEmailService;
    }

    public void sendCode(String email) {
        VerificationRecord existing = cache.get(email);
        Instant now = Instant.now();
        if (existing != null && now.isBefore(existing.lastSentAt().plus(RESEND_INTERVAL))) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        cache.put(email, new VerificationRecord(code, now.plus(CODE_TTL), now));
        resendEmailService.sendVerificationCode(email, code);
    }

    public void verifyCodeOrThrow(String email, String code) {
        VerificationRecord record = cache.get(email);
        if (record == null) {
            throw new BusinessException("请先获取邮箱验证码");
        }
        if (Instant.now().isAfter(record.expiredAt())) {
            cache.remove(email);
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!record.code().equals(code)) {
            throw new BusinessException("验证码错误");
        }
        cache.remove(email);
    }

    private record VerificationRecord(String code, Instant expiredAt, Instant lastSentAt) {
    }
}