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
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface AdminRepository extends CrudRepository<Admin, Long> {

    // 通过用户名查询管理员信息
    @Query("""
                select new org.example.backend.common.entity.Admin(
                   u.email, u.password
                )
                from Admin u
                where u.username = :username
            """)
    Admin findByUsername(String username);




}
