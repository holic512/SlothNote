/**
 * File Name: StatusEnum.java
 * Description: 用于 用户账号状态枚举
 * Author: holic512
 * Created Date: 2024-09-04
 * Version: 1.0
 * Usage:
 * 区分 用户 正常 停用 封禁 等行为
 */
package org.example.backend.common.enums;

public enum UserStatusEnum {
    ACTIVE,    // 用户正常状态
    DISABLED,  // 用户被停用
    BANNED;    // 用户被封禁
}