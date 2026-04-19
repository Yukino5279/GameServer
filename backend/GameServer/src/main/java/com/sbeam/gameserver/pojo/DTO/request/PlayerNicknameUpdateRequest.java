package com.sbeam.gameserver.pojo.DTO.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PlayerNicknameUpdateRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "邮箱长度不能超过255")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度应在6-64之间")
    private String password;

    @NotBlank(message = "新昵称不能为空")
    @Size(min = 2, max = 32, message = "昵称长度应在2-32之间")
    private String newNickname;
}