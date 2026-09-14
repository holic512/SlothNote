import {afterEach, describe, expect, it, vi} from 'vitest';

const loadHttpModule = async () => {
    vi.resetModules();
    return import('./index');
};

afterEach(() => {
    vi.unstubAllEnvs();
    vi.resetModules();
});

describe('HTTP authentication routing', () => {
    it.each([
        ['admin/users', 'admin'],
        ['/admin/users', 'admin'],
        ['user/note/SaveNote', 'user'],
        ['/user/note/SaveNote', 'user'],
        ['https://api.example.test/user/note/SaveNote', null],
        ['', null],
        ['auth/login', null],
        ['api/user/note/SaveNote', null],
        [undefined, null],
    ] as const)('maps request URL %s to %s', async (url, expectedRole) => {
        const {getRequestAuthRole} = await loadHttpModule();

        expect(getRequestAuthRole(url)).toBe(expectedRole);
    });

    it('normalizes an environment-provided API base URL at module initialization', async () => {
        vi.stubEnv('VITE_API_BASE_URL', ' https://api.example.test/v1 ');

        const firstModule = await loadHttpModule();
        expect(firstModule.API_BASE_URL).toBe('https://api.example.test/v1/');

        vi.stubEnv('VITE_API_BASE_URL', 'https://second.example.test');
        const secondModule = await loadHttpModule();
        expect(secondModule.API_BASE_URL).toBe('https://second.example.test/');
    });

    it('only maps absolute URLs that are inside the configured API base', async () => {
        vi.stubEnv('VITE_API_BASE_URL', 'https://api.example.test/v1/');
        const {getRequestAuthRole} = await loadHttpModule();

        expect(getRequestAuthRole('https://api.example.test/v1/admin/users')).toBe('admin');
        expect(getRequestAuthRole('https://api.example.test/v1/user/notes')).toBe('user');
        expect(getRequestAuthRole('https://api.example.test/admin/users')).toBeNull();
        expect(getRequestAuthRole('https://evil.example.test/v1/user/notes')).toBeNull();
    });
});
