package com.sbeam.gameserver.service;


import com.sbeam.gameserver.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.util.HtmlUtils;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendVerificationCode(String toEmail, String code) {
        Assert.hasText(toEmail, "收件人邮箱不能为空");
        Assert.hasText(code, "验证码不能为空");

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("GameServer 邮箱验证码");
            helper.setText(buildEmailHtml(HtmlUtils.htmlEscape(code)), true); // true = 发HTML

            mailSender.send(message);
            log.info("验证码邮件发送成功: to={}", maskEmail(toEmail));
            return CompletableFuture.completedFuture(null);

        } catch (MailException | MessagingException e) {
            log.error("验证码邮件发送失败: to={}, reason={}", maskEmail(toEmail), e.getMessage());
            return CompletableFuture.failedFuture(new BusinessException("发送验证码失败，请稍后重试"));
        }
    }

    private String buildEmailHtml(String code) {
        return """
            <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
              <h2 style="font-size: 18px;">GameServer 邮箱验证</h2>
              <p>您的验证码是：</p>
              <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                          padding: 16px; background: #f5f5f5; text-align: center;">
                %s
              </div>
              <p style="color: #888; font-size: 12px;">
                验证码 5 分钟内有效，请勿泄露给他人。
              </p>
            </div>
            """.formatted(code);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 2) return "***" + email.substring(at);
        return email.charAt(0) + "***" + email.substring(at - 1);
    }
}