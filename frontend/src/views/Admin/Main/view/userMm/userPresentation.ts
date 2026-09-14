export interface UserPresentationData {
    username?: unknown
    avatar?: unknown
}

const FALLBACK_USER_NAME = '未命名用户';

export const getUserDisplayName = (user?: UserPresentationData | null): string => {
    const username = user?.username;
    return typeof username === 'string' && username.trim() ? username : FALLBACK_USER_NAME;
};

export const getUserAvatarFallback = (user?: UserPresentationData | null): string => (
    getUserDisplayName(user).charAt(0).toUpperCase()
);

export const getUserAvatarUrl = (user?: UserPresentationData | null): string => (
    typeof user?.avatar === 'string' ? user.avatar : ''
);
