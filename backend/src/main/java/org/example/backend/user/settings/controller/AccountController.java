// /**
//  * 文件名称: accountController.java
//  * 描述: 该控制器处理用户账户设置操作，包括获取用户的个人资料信息。
//  * 作者: holic512
//  * 创建日期: 2024-10-12
//  * 版本: 1.0
//  * 用法: 该类提供了一个API端点，用于根据用户的登录信息获取用户账户资料。
//  */
//
// package org.example.backend.user.settings.controller;
//
// import io.swagger.v3.oas.annotations.tags.Tag;
// import jakarta.validation.Valid;
// import org.example.backend.common.entity.UserProfile;
// import org.example.backend.common.response.ApiResponse;
// import org.example.backend.common.util.StpKit;
// import org.example.backend.user.settings.dto.AccountInfo;
// import org.example.backend.user.settings.service.AccountService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;
//
// @Tag(name = "用户-账户信息", description = "用于修改用户的账户信息")
// @RestController
// @RequestMapping("/user/settings/account")
// public class AccountController {
//
//     private final AccountService accountService;
//
//     @Autowired
//     public AccountController(final AccountService accountService) {
//         this.accountService = accountService;
//     }
//
//     @GetMapping("userAllProfile")
//     public ResponseEntity<Object> userAllProfile() throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         AccountInfo accountInfo = accountService.fetchAccountInfo(uid);
//
//
//         return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                 .status(200)
//                 .message("查询成功")
//                 .data(accountInfo)
//                 .build()
//         );
//     }
//
//     @PutMapping("nickname")
//     public ResponseEntity<Object> EditUserNickName(@RequestBody UserProfile updateProfile) throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         try{
//             AccountInfo updateAccountNickName = accountService.EditAccountNickName(uid, updateProfile);
//
//             return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                     .status(200)
//                     .message("用户信息更新成功")
//                     .data(updateAccountNickName)
//                     .build()
//             );
//         }catch (Exception e){
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ApiResponse.Builder<String>()
//                             .status(500)
//                             .message("用户信息更新失败")
//                             .build()
//                     );
//         }
//     }
//
//     @PutMapping("gender")
//     public ResponseEntity<Object> EditUserGender(@RequestBody UserProfile updateProfile) throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         try{
//             AccountInfo updateAccountGender = accountService.EditAccountGender(uid, updateProfile);
//
//             return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                     .status(200)
//                     .message("用户信息更新成功")
//                     .data(updateAccountGender)
//                     .build()
//             );
//         }catch (Exception e){
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ApiResponse.Builder<String>()
//                             .status(500)
//                             .message("用户信息更新失败")
//                             .build()
//                     );
//         }
//     }
//
//     @PutMapping("age")
//     public ResponseEntity<Object> EditUserAge(@RequestBody UserProfile updateProfile) throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         try{
//             AccountInfo updateAccountAge = accountService.EditAccountAge(uid, updateProfile);
//
//             return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                     .status(200)
//                     .message("用户信息更新成功")
//                     .data(updateAccountAge)
//                     .build()
//             );
//         }catch (Exception e){
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ApiResponse.Builder<String>()
//                             .status(500)
//                             .message("用户信息更新失败")
//                             .build()
//                     );
//         }
//     }
//
//     @PutMapping("contactInfo")
//     public ResponseEntity<Object> EditUserContactInfo(@RequestBody UserProfile updateProfile) throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         try{
//             AccountInfo updateAccountContact = accountService.EditAccountContact(uid, updateProfile);
//
//             return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                     .status(200)
//                     .message("用户信息更新成功")
//                     .data(updateAccountContact)
//                     .build()
//             );
//         }catch (Exception e){
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ApiResponse.Builder<String>()
//                             .status(500)
//                             .message("用户信息更新失败")
//                             .build()
//                     );
//         }
//     }
//
//     @PutMapping("bio")
//     public ResponseEntity<Object> EditUserBio(@RequestBody UserProfile updateProfile) throws Exception {
//         // 获取uid
//         String uid = (String) StpKit.USER.getLoginId();
//
//         try{
//             AccountInfo updateAccountBio = accountService.EditAccountBio(uid, updateProfile);
//
//             return ResponseEntity.ok(new ApiResponse.Builder<AccountInfo>()
//                     .status(200)
//                     .message("用户信息更新成功")
//                     .data(updateAccountBio)
//                     .build()
//             );
//         }catch (Exception e){
//             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                     .body(new ApiResponse.Builder<String>()
//                             .status(500)
//                             .message("用户信息更新失败")
//                             .build()
//                     );
//         }
//     }
// }
