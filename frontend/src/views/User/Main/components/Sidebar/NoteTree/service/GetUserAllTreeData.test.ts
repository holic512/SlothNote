import {beforeEach, describe, expect, it, vi} from 'vitest';
import type {Tree} from '../interface/treeInterface';

const serviceMocks = vi.hoisted(() => ({
    currentToken: 'user-a' as string | undefined,
    axiosGet: vi.fn(),
}));

vi.mock('../../../../../../../axios', () => ({
    default: {
        get: serviceMocks.axiosGet,
    },
}));

vi.mock('@/pinia/token', () => ({
    tokenStore: () => ({
        getUserToken: () => serviceMocks.currentToken,
    }),
}));

import {
    clearUserAllTreeDataCache,
    getUserAllTreeData,
    patchNoteTreeNodes,
    patchUserAllTreeDataCache,
} from './GetUserAllTreeData';

interface Deferred<T> {
    promise: Promise<T>;
    resolve: (value: T) => void;
}

const createDeferred = <T>(): Deferred<T> => {
    let resolve!: (value: T) => void;
    const promise = new Promise<T>((promiseResolve) => {
        resolve = promiseResolve;
    });
    return {promise, resolve};
};

const createTree = (prefix: string): Tree[] => ([
    {
        id: 1,
        label: `${prefix}-folder`,
        type: 'FOLDER',
        children: [
            {
                id: 11,
                label: `${prefix}-note`,
                type: 'NOTE',
                cover: null,
            },
        ],
    },
    {
        id: 2,
        label: `${prefix}-standalone`,
        type: 'NOTE',
    },
]);

describe('note tree patching and cache isolation', () => {
    beforeEach(() => {
        clearUserAllTreeDataCache();
        serviceMocks.currentToken = 'user-a';
        serviceMocks.axiosGet.mockReset();
    });

    it('patches a nested matching node without mutating the source tree', () => {
        const source = createTree('original');
        const untouchedNode = source[1];

        const result = patchNoteTreeNodes(source, 'NOTE', 11, {
            label: 'renamed-note',
            cover: 'cover.png',
        });

        expect(result).not.toBe(source);
        expect(result[0].children?.[0]).toMatchObject({
            id: 11,
            label: 'renamed-note',
            cover: 'cover.png',
        });
        expect(source[0].children?.[0]).toMatchObject({
            label: 'original-note',
            cover: null,
        });
        expect(result[1]).toBe(untouchedNode);
    });

    it('reuses the original tree when no node matches the patch', () => {
        const source = createTree('original');

        const result = patchNoteTreeNodes(source, 'NOTE', 999, {label: 'missing'});

        expect(result).toBe(source);
        expect(result[0].children).toBe(source[0].children);
    });

    it('reuses cached data for the same token and refetches after an account switch', async () => {
        serviceMocks.axiosGet
            .mockResolvedValueOnce({data: {data: createTree('account-a')}})
            .mockResolvedValueOnce({data: {data: createTree('account-b')}});

        const firstResult = await getUserAllTreeData();
        const cachedResult = await getUserAllTreeData();

        expect(cachedResult).toBe(firstResult);
        expect(firstResult[0]).toMatchObject({
            label: 'account-a-folder',
            uniqueId: 'FOLDER_1',
        });
        expect(firstResult[0].children?.[0].uniqueId).toBe('NOTE_11');
        expect(serviceMocks.axiosGet).toHaveBeenCalledTimes(1);

        serviceMocks.currentToken = 'user-b';
        expect(patchUserAllTreeDataCache('NOTE', 11, {label: 'cross-account-change'})).toBeNull();

        const secondResult = await getUserAllTreeData();
        expect(secondResult[0].label).toBe('account-b-folder');
        expect(serviceMocks.axiosGet).toHaveBeenCalledTimes(2);
    });

    it('deduplicates concurrent initial loads for the same account', async () => {
        const deferred = createDeferred<{data: {data: Tree[]}}>();
        serviceMocks.axiosGet.mockReturnValueOnce(deferred.promise);

        const firstRequest = getUserAllTreeData();
        const secondRequest = getUserAllTreeData();

        expect(serviceMocks.axiosGet).toHaveBeenCalledTimes(1);

        deferred.resolve({data: {data: createTree('shared')}});
        const [firstResult, secondResult] = await Promise.all([firstRequest, secondRequest]);

        expect(secondResult).toBe(firstResult);
    });

    it('does not let an older response overwrite a newer forced refresh', async () => {
        const older = createDeferred<{data: {data: Tree[]}}>();
        const newer = createDeferred<{data: {data: Tree[]}}>();
        serviceMocks.axiosGet
            .mockReturnValueOnce(older.promise)
            .mockReturnValueOnce(newer.promise);

        const olderRequest = getUserAllTreeData();
        const newerRequest = getUserAllTreeData(true);

        newer.resolve({data: {data: createTree('newer')}});
        const newerResult = await newerRequest;
        expect(newerResult[0].label).toBe('newer-folder');

        older.resolve({data: {data: createTree('older')}});
        const olderResult = await olderRequest;
        expect(olderResult[0].label).toBe('older-folder');

        const cachedResult = await getUserAllTreeData();
        expect(cachedResult[0].label).toBe('newer-folder');
        expect(serviceMocks.axiosGet).toHaveBeenCalledTimes(2);
    });
});
