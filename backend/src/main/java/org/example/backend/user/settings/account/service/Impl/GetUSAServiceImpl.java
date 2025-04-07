/**
 * File Name: GetUSAServiceImpl.java
 * Description: Todo
 * Author: holic512
 * Created Date: 2024-12-23
 * Version: 1.0
 * Usage:
 * Todo
 */
package org.example.backend.user.settings.account.service.Impl;

import lombok.RequiredArgsConstructor;
import org.example.backend.user.settings.account.repository.USAUserProfilesRep;
import org.example.backend.user.settings.account.repository.USAUserRep;
import org.example.backend.user.settings.account.service.GetUSAService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUSAServiceImpl implements GetUSAService {

    private final USAUserProfilesRep userProfilesRep;
    private final USAUserRep userRep;

    @Override
    public boolean getHasProfile(Long userId) {
        Integer result = userRep.findHasProfileById(userId);
        return result != null &&result == 1;
    }
}
