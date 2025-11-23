/**
 * File Name: UserProfileRepository.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-09-20
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.common.repository;

import org.example.backend.common.dto.user.UserAuthDto;
import org.example.backend.common.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    @Query("SELECT up from UserProfile up where up.userId = :UserId")
    UserProfile findByUserId(@Param("UserId") Long UserId);

}
