/**
 * File Name: USAUserRep.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-12-23
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.settings.account.repository;

import org.example.backend.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface USAUserRep extends JpaRepository<User,Long> {

    @Query("select u.hasProfile from User u where  u.id = :userId")
    Integer findHasProfileById(@Param("userId") Long userId);
}
