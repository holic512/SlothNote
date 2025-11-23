package org.example.backend.admin.repository;

import org.example.backend.admin.userMm.dto.OUserInfoDto;
import org.example.backend.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdminUserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("SELECT new org.example.backend.admin.userMm.dto.OUserInfoDto(u.id,u.uid,u.username, u.email) FROM User u WHERE u.uid IN :uids")
    List<OUserInfoDto> findUsernamesAndEmailsByUidList(@Param("uids") List<String> uids);

}