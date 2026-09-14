import {describe, expect, it} from 'vitest';
import {getAuthRoleByPath, resolveAuthRedirect} from './authGuard';
import {ROUTE_PATHS} from './paths';

describe('authGuard', () => {
    it.each([
        ['/user/main', 'user'],
        ['/user/main/edit', 'user'],
        ['/admin/main', 'admin'],
        ['/admin/main/users', 'admin'],
        ['/', null],
        ['/pd', null],
    ] as const)('maps %s to the expected auth role', (path, expectedRole) => {
        expect(getAuthRoleByPath(path)).toBe(expectedRole);
    });

    it('allows public routes without inspecting tokens', () => {
        expect(resolveAuthRedirect(
            {path: ROUTE_PATHS.userLogin, requiresAuth: false},
            {},
        )).toBeNull();
    });

    it('redirects a user route to the canonical user login path when its token is absent', () => {
        expect(resolveAuthRedirect(
            {path: ROUTE_PATHS.userMain, requiresAuth: true},
            {adminToken: 'admin-token'},
        )).toBe(ROUTE_PATHS.userLogin);
    });

    it('redirects an admin route to the admin login path when its token is absent', () => {
        expect(resolveAuthRedirect(
            {path: ROUTE_PATHS.adminMain, requiresAuth: true},
            {userToken: 'user-token'},
        )).toBe(ROUTE_PATHS.adminLogin);
    });

    it('allows protected routes only when the matching role token exists', () => {
        expect(resolveAuthRedirect(
            {path: ROUTE_PATHS.userEdit, requiresAuth: true},
            {userToken: 'user-token'},
        )).toBeNull();
        expect(resolveAuthRedirect(
            {path: ROUTE_PATHS.adminMain, requiresAuth: true},
            {adminToken: 'admin-token'},
        )).toBeNull();
    });
});
