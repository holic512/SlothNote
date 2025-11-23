package org.example.backend.user.settings.account.service;

import org.example.backend.common.entity.User;
import org.example.backend.common.entity.UserProfile;
import org.example.backend.common.util.StpKit;
import org.example.backend.common.util.SCryptUtil;
import org.example.backend.common.util.file.LocalFileStorage;
import org.example.backend.user.repository.UserUserProfileRepository;
import org.example.backend.user.repository.UserUserRepository;
import org.example.backend.user.settings.account.dto.UserAllProfileDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
public class AccountSettingsService {
    private final UserUserRepository userRepository;
    private final UserUserProfileRepository profileRepository;
    private final LocalFileStorage fileStorage;

    public AccountSettingsService(UserUserRepository userRepository, UserUserProfileRepository profileRepository, LocalFileStorage fileStorage) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.fileStorage = fileStorage;
    }

    public User getCurrentUser(String token) {
        Object idObj = StpKit.USER.getSession().get("id");
        if (idObj instanceof Long) {
            Long id = (Long) idObj;
            return userRepository.findById(id).orElse(null);
        }
        if (StpKit.USER.isLogin()) {
            String uid = (String) StpKit.USER.getLoginId();
            User u = userRepository.findByUid(uid);
            if (u != null) return u;
        }
        if (token != null && !token.isEmpty()) {
            return userRepository.findByUid(token);
        }
        return null;
    }

    public UserProfile getOrCreateProfile(Long userId) {
        Optional<UserProfile> opt = profileRepository.findByUserId(userId);
        if (opt.isPresent()) return opt.get();
        UserProfile p = new UserProfile();
        p.setUserId(userId);
        p.setNickname("");
        p.setGender("");
        return profileRepository.save(p);
    }

    public UserAllProfileDTO getAllProfile(String token) {
        User user = getCurrentUser(token);
        Long userId = user != null ? user.getId() : (Long) StpKit.USER.getSession().get("id");
        UserProfile profile = getOrCreateProfile(userId);
        UserAllProfileDTO dto = new UserAllProfileDTO();
        if (user != null) {
            dto.setId(user.getId());
            dto.setUid(user.getUid());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
        } else {
            dto.setId(userId);
        }
        dto.setNickname(profile.getNickname());
        dto.setGender(profile.getGender());
        dto.setAge(profile.getAge());
        dto.setContactInfo(profile.getContactInfo());
        dto.setBio(profile.getBio());
        String avatar = profile.getAvatar();
        String url = avatar == null ? null : fileStorage.resolveUrl(avatar);
        dto.setAvatar(url == null ? avatar : url);
        return dto;
    }

    public void updateNickname(String token, String nickname) {
        User user = getCurrentUser(token);
        UserProfile p = getOrCreateProfile(user.getId());
        p.setNickname(nickname);
        profileRepository.save(p);
    }

    public void updateGender(String token, String gender) {
        User user = getCurrentUser(token);
        UserProfile p = getOrCreateProfile(user.getId());
        p.setGender(gender);
        profileRepository.save(p);
    }

    public void updateAge(String token, Integer age) {
        User user = getCurrentUser(token);
        UserProfile p = getOrCreateProfile(user.getId());
        p.setAge(age);
        profileRepository.save(p);
    }

    public void updateContactInfo(String token, String contactInfo) {
        User user = getCurrentUser(token);
        UserProfile p = getOrCreateProfile(user.getId());
        p.setContactInfo(contactInfo);
        profileRepository.save(p);
    }

    public void updateBio(String token, String bio) {
        User user = getCurrentUser(token);
        UserProfile p = getOrCreateProfile(user.getId());
        p.setBio(bio);
        profileRepository.save(p);
    }

    public String uploadAvatar(String token, MultipartFile file) throws IOException {
        User user = getCurrentUser(token);
        Long userId = user != null ? user.getId() : (Long) StpKit.USER.getSession().get("id");
        UserProfile p = getOrCreateProfile(userId);
        String tag = fileStorage.save(file, "avatar");
        p.setAvatar(tag);
        profileRepository.save(p);
        String url = fileStorage.resolveUrl(tag);
        return url == null ? tag : url;
    }

    public boolean changePassword(String token, String oldPassword, String newPassword) {
        User user = getCurrentUser(token);
        Long userId = user != null ? user.getId() : (Long) StpKit.USER.getSession().get("id");
        if (user == null && userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }
        if (user == null) return false;
        if (!SCryptUtil.verifyPassword(oldPassword, user.getPassword())) return false;
        String hashed = SCryptUtil.hashPassword(newPassword);
        user.setPassword(hashed);
        userRepository.save(user);
        return true;
    }
}