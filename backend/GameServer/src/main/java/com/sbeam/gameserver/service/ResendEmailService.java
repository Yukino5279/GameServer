package com.sbeam.gameserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbeam.gameserver.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

@Service
public class ResendEmailService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String apiKey;
    private final String fromEmail;

    public ResendEmailService(
            ObjectMapper objectMapper,
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from-email:}") String fromEmail
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
    }

    public void sendVerificationCode(String toEmail, String code) {
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            throw new BusinessException("邮件服务未配置，请设置 resend.api-key 和 resend.from-email");
        }

        Map<String, Object> payload = Map.of(
                "from", fromEmail,
                "to", toEmail,
                "subject", "GameServer 邮箱验证码",
                "html", "<p>你的验证码是：<b>" + code + "</b></p><p>5分钟内有效。</p>"
        );

        try {
            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new BusinessException("发送验证码失败，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("发送验证码失败，请稍后重试");
        } catch (IOException e) {
            throw new BusinessException("发送验证码失败，请稍后重试");
        }
    }
}