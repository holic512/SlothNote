package org.example.backend.admin.userMm.request;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private Long id;
    private String username;
    private String email;
    private String password;
    private Integer status;
    private String nickname;
    private String gender;
    private Integer age;
    private String bio;
    private String contactInfo;
    private String avatar;
}