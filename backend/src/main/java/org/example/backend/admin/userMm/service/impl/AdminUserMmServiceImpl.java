/**
 * File Name: AdminUserMmServiceImpl.java
 * Description: 管理员 用户管理 服务类接口 的 实现类
 * Author: holic512
 * Created Date: 2024-09-12
 * Version: 1.0
 * Usage:
 * 管理员 用户管理 服务类接口 的 实现类
 */
package org.example.backend.admin.userMm.service.impl;

import org.example.backend.admin.repository.AdminUserRepository;
import org.example.backend.admin.userMm.dto.AdminUserDetailDto;
import org.example.backend.admin.userMm.enums.AdminUserMmEnum;
import org.example.backend.admin.userMm.request.AddUserRequest;
import org.example.backend.admin.userMm.request.SearchUserRequest;
import org.example.backend.admin.userMm.request.UpdateUserRequest;
import org.example.backend.admin.userMm.service.AdminUserMmService;
import org.example.backend.common.dto.user.UserDetailDto;
import org.example.backend.common.entity.User;
import org.example.backend.common.entity.UserProfile;
import org.example.backend.common.enums.user.UserStatusEnum;
import org.example.backend.common.repository.UserRepository;
import org.example.backend.common.util.SCryptUtil;
import org.example.backend.common.util.UuidUtil;
import org.example.backend.common.util.file.LocalFileStorage;
import org.example.backend.user.repository.UserUserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminUserMmServiceImpl implements AdminUserMmService {

    private final UserRepository userRepository;
    private final AdminUserRepository adminUserRepository;

    private final UserUserProfileRepository userProfileRepository;
    private final LocalFileStorage fileStorage;

    @Autowired
    public AdminUserMmServiceImpl(UserRepository userRepository, UserUserProfileRepository userProfileRepository, LocalFileStorage fileStorage, AdminUserRepository adminUserRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.fileStorage = fileStorage;
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public Long getUserCount() {
        return userRepository.count();
    }

    @Override
    public List<UserDetailDto> fetchInitialUser(int count) {
        // 判断 查询范围
        Pageable pageable = PageRequest.of(0, count);

        // 查询数据库中用户信息
        List<UserDetailDto> users = userRepository.findUserInRange(pageable);

        for (UserDetailDto user : users) {
            Optional<UserProfile> p = userProfileRepository.findByUserId(user.getId());
            String raw = p.map(UserProfile::getAvatar).orElse(null);
            String url = raw == null ? null : fileStorage.resolveUrl(raw);
            user.setAvatar(url == null ? raw : url);
        }

        return users;
    }

    @Override
    public List<UserDetailDto> searchUsers(SearchUserRequest request) {
        int page = request.getPageNum() == null ? 0 : Math.max(0, request.getPageNum() - 1);
        int size = request.getPageSize() == null ? 10 : request.getPageSize();
        Pageable pageable = PageRequest.of(page, size);
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getQ() != null && !request.getQ().isEmpty()) {
                String like = "%" + request.getQ().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("email"), like),
                        cb.like(root.get("uid"), like)
                ));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getGender() != null && !request.getGender().isEmpty()) {
                Subquery<Long> sq = query.subquery(Long.class);
                var pRoot = sq.from(UserProfile.class);
                sq.select(pRoot.get("userId")).where(cb.and(
                        cb.equal(pRoot.get("gender"), request.getGender()),
                        cb.equal(pRoot.get("userId"), root.get("id"))
                ));
                predicates.add(cb.exists(sq));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<User> pageData = adminUserRepository.findAll(spec, pageable);
        List<UserDetailDto> result = new ArrayList<>();
        for (User u : pageData.getContent()) {
            UserDetailDto dto = new UserDetailDto(u.getId(), u.getUid(), u.getUsername(), u.getEmail(), u.getStatus());
            Optional<UserProfile> p = userProfileRepository.findByUserId(u.getId());
            String raw = p.map(UserProfile::getAvatar).orElse(null);
            String url = raw == null ? null : fileStorage.resolveUrl(raw);
            dto.setAvatar(url == null ? raw : url);
            result.add(dto);
        }
        return result;
    }

    @Override
    public long countSearchUsers(SearchUserRequest request) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.getQ() != null && !request.getQ().isEmpty()) {
                String like = "%" + request.getQ().trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("username"), like),
                        cb.like(root.get("email"), like),
                        cb.like(root.get("uid"), like)
                ));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getGender() != null && !request.getGender().isEmpty()) {
                Subquery<Long> sq = query.subquery(Long.class);
                var pRoot = sq.from(UserProfile.class);
                sq.select(pRoot.get("userId")).where(cb.and(
                        cb.equal(pRoot.get("gender"), request.getGender()),
                        cb.equal(pRoot.get("userId"), root.get("id"))
                ));
                predicates.add(cb.exists(sq));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return adminUserRepository.count(spec);
    }

    @Override
    public AdminUserDetailDto getUserDetail(Long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isEmpty()) return null;
        AdminUserDetailDto dto = new AdminUserDetailDto();
        dto.setId(u.get().getId());
        dto.setUid(u.get().getUid());
        dto.setUsername(u.get().getUsername());
        dto.setEmail(u.get().getEmail());
        dto.setStatus(u.get().getStatus());
        Optional<UserProfile> p = userProfileRepository.findByUserId(u.get().getId());
        p.ifPresent(profile -> {
            dto.setNickname(profile.getNickname());
            dto.setGender(profile.getGender());
            dto.setAge(profile.getAge());
            dto.setBio(profile.getBio());
            dto.setContactInfo(profile.getContactInfo());
            String raw = profile.getAvatar();
            String url = raw == null ? null : fileStorage.resolveUrl(raw);
            dto.setAvatar(url == null ? raw : url);
        });
        return dto;
    }

    @Override
    public AdminUserMmEnum updateUser(UpdateUserRequest request) {
        Optional<User> uo = userRepository.findById(request.getId());
        if (uo.isEmpty()) return AdminUserMmEnum.Success;
        User u = uo.get();
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (!u.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
                return AdminUserMmEnum.UserAlreadyExists;
            }
            u.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (!u.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                return AdminUserMmEnum.EmailAlreadyExists;
            }
            u.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            u.setPassword(SCryptUtil.hashPassword(request.getPassword()));
        }
        if (request.getStatus() != null) {
            u.setStatus(request.getStatus());
        }
        userRepository.save(u);
        Optional<UserProfile> po = userProfileRepository.findByUserId(u.getId());
        UserProfile p = po.orElseGet(UserProfile::new);
        if (po.isEmpty()) p.setUserId(u.getId());
        if (request.getNickname() != null) p.setNickname(request.getNickname());
        if (request.getGender() != null) p.setGender(request.getGender());
        if (request.getAge() != null) p.setAge(request.getAge());
        if (request.getBio() != null) p.setBio(request.getBio());
        if (request.getContactInfo() != null) p.setContactInfo(request.getContactInfo());
        if (request.getAvatar() != null) p.setAvatar(request.getAvatar());
        if (request.getNickname() == null && po.isEmpty()) p.setNickname(u.getUsername());
        if (request.getGender() == null && po.isEmpty()) p.setGender("secret");
        userProfileRepository.save(p);
        return AdminUserMmEnum.Success;
    }

    @Override
    public boolean deleteUser(Long id) {
        try {
            userRepository.deleteById(id);
            Optional<UserProfile> po = userProfileRepository.findByUserId(id);
            po.ifPresent(userProfileRepository::delete);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean batchUpdateStatus(List<Long> userIds, UserStatusEnum status) {
        try {
            userRepository.updateStatusByIds(userIds, status.getValue());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<UserDetailDto> findUserInRange(int start, int end) {
        // 判断 查询范围
        Pageable pageable = PageRequest.of(start, end);

        // 查询数据库中用户信息
        List<UserDetailDto> users = userRepository.findUserInRange(pageable);

        for (UserDetailDto user : users) {
            Optional<UserProfile> p = userProfileRepository.findByUserId(user.getId());
            String raw = p.map(UserProfile::getAvatar).orElse(null);
            String url = raw == null ? null : fileStorage.resolveUrl(raw);
            user.setAvatar(url == null ? raw : url);
        }
        return users;
    }

    @Override
    public AdminUserMmEnum addUser(AddUserRequest addUserRequest) {

        // 查询数据库中是否存在此用户名 或 邮箱
        boolean existUsername = userRepository.existsByUsername(addUserRequest.getUsername());
        if (existUsername) {
            return AdminUserMmEnum.UserAlreadyExists;
        }
        boolean existEmail = userRepository.existsByEmail(addUserRequest.getEmail());
        if (existEmail) {
            return AdminUserMmEnum.EmailAlreadyExists;
        }

        // 生成不重复的UID
        String uid;      // 获取uid
        do {
            uid = UuidUtil.getUid();
        } while (userRepository.existsByUid(uid));

        // 密码加密
        String password = SCryptUtil.hashPassword(addUserRequest.getPassword());

        // 整合到User类中
        User user = new User.Builder()
                .uid(uid)
                .username(addUserRequest.getUsername())
                .password(password)
                .email(addUserRequest.getEmail())
                .status(addUserRequest.getStatus().getValue())
                .build();

        // success
        userRepository.save(user);

        return AdminUserMmEnum.Success;
    }

    @Override
    public boolean deleteUsers(List<Long> userIds) {
        try {
            userRepository.deleteUsersByIds(userIds);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}






