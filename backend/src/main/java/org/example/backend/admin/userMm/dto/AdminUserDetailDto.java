package org.example.backend.admin.userMm.dto;

import lombok.Data;

@Data
public class AdminUserDetailDto {
    private Long id;
    private String uid;
    private String username;
    private String email;
    private Integer status;
    private String nickname;
    private String gender;
    private Integer age;
    private String bio;
    private String contactInfo;
    private String avatar;
}