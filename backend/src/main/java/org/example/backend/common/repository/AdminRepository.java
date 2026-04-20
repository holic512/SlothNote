/**
 * File Name: AdminRepository.java
 * Description: 管理员 持久层接口
 * Author: holic512
 * Created Date: 2024-09-04
 * Version: 1.0
 * Usage:
 * 管理员 的 持久层接口
 */
package org.example.backend.common.repository;

import org.example.backend.common.entity.Admin;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AdminRepository extends CrudRepository<Admin, Long> {

    // 通过用户名查询管理员信息
    Admin findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByIsDeleted(Integer isDeleted);

    Optional<Admin> findByIdAndIsDeleted(Long id, Integer isDeleted);

}
