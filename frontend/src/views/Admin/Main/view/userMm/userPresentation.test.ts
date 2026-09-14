import {describe, expect, it} from 'vitest';
import {getUserAvatarFallback, getUserAvatarUrl, getUserDisplayName} from './userPresentation';

describe('user table presentation', () => {
    it('uses a readable fallback when the table slot has no user row', () => {
        expect(getUserDisplayName()).toBe('未命名用户');
        expect(getUserAvatarFallback()).toBe('未');
        expect(getUserAvatarUrl()).toBe('');
    });

    it('uses the username and avatar when both are valid', () => {
        const user = {username: 'alice', avatar: 'https://example.com/alice.png'};

        expect(getUserDisplayName(user)).toBe('alice');
        expect(getUserAvatarFallback(user)).toBe('A');
        expect(getUserAvatarUrl(user)).toBe('https://example.com/alice.png');
    });

    it('does not call string methods on missing or non-string usernames', () => {
        expect(getUserDisplayName({username: undefined})).toBe('未命名用户');
        expect(getUserAvatarFallback({username: null})).toBe('未');
        expect(getUserAvatarUrl({avatar: null})).toBe('');
    });
});
