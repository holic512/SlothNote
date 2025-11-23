package org.example.backend.user.settings.account.controller;

import org.example.backend.common.response.ApiResponse;
import org.example.backend.user.settings.account.dto.*;
import org.example.backend.user.settings.account.service.AccountSettingsService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/user/settings/account")
public class AccountSettingsController {
    private final AccountSettingsService service;

    public AccountSettingsController(AccountSettingsService service) {
        this.service = service;
    }

    @GetMapping("/userAllProfile")
    public ApiResponse<UserAllProfileDTO> getAll(@RequestHeader("satoken") String token) {
        UserAllProfileDTO data = service.getAllProfile(token);
        return new ApiResponse.Builder<UserAllProfileDTO>().status(200).message("ok").data(data).build();
    }

    @PutMapping("/nickname")
    public ApiResponse<Void> nickname(@RequestHeader("satoken") String token, @RequestBody NicknameDTO body) {
        service.updateNickname(token, body.getNickname());
        return new ApiResponse.Builder<Void>().status(200).message("ok").build();
    }

    @PutMapping("/gender")
    public ApiResponse<Void> gender(@RequestHeader("satoken") String token, @RequestBody GenderDTO body) {
        service.updateGender(token, body.getGender());
        return new ApiResponse.Builder<Void>().status(200).message("ok").build();
    }

    @PutMapping("/age")
    public ApiResponse<Void> age(@RequestHeader("satoken") String token, @RequestBody AgeDTO body) {
        service.updateAge(token, body.getAge());
        return new ApiResponse.Builder<Void>().status(200).message("ok").build();
    }

    @PutMapping("/contactInfo")
    public ApiResponse<Void> contactInfo(@RequestHeader("satoken") String token, @RequestBody ContactInfoDTO body) {
        service.updateContactInfo(token, body.getContactInfo());
        return new ApiResponse.Builder<Void>().status(200).message("ok").build();
    }

    @PutMapping("/bio")
    public ApiResponse<Void> bio(@RequestHeader("satoken") String token, @RequestBody BioDTO body) {
        service.updateBio(token, body.getBio());
        return new ApiResponse.Builder<Void>().status(200).message("ok").build();
    }

    @PostMapping(path = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> avatar(@RequestHeader("satoken") String token, @RequestPart("file") MultipartFile file) throws IOException {
        String url = service.uploadAvatar(token, file);
        return new ApiResponse.Builder<String>().status(200).message("ok").data(url).build();
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestHeader("satoken") String token, @RequestBody Map<String, String> body) {
        String oldPw = body.get("oldPassword");
        String newPw = body.get("newPassword");
        boolean ok = service.changePassword(token, oldPw, newPw);
        return ok
                ? new ApiResponse.Builder<Void>().status(200).message("ok").build()
                : new ApiResponse.Builder<Void>().status(400).message("incorrect").build();
    }
}