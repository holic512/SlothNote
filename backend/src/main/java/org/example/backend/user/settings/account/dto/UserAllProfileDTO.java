package org.example.backend.user.settings.account.dto;

public class UserAllProfileDTO {
    private Long id;
    private String avatar;
    private String nickname;
    private String uid;
    private String username;
    private String email;
    private String gender;
    private Integer age;
    private String contactInfo;
    private String bio;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}