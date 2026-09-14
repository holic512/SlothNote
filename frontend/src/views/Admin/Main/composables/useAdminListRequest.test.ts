import {describe, expect, it, vi} from 'vitest';
import {getMaxPage, useLatestRequest} from './useAdminListRequest';

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

describe('getMaxPage', () => {
    it.each([
        [0, 10, 1],
        [-5, 10, 1],
        [1, 10, 1],
        [10, 10, 1],
        [11, 10, 2],
        [25, 10, 3],
        [25, 0, 25],
    ])('total=%i and pageSize=%i returns %i pages', (total, pageSize, expected) => {
        expect(getMaxPage(total, pageSize)).toBe(expected);
    });
});

describe('useLatestRequest', () => {
    it('aborts an older request and only commits the newest result', async () => {
        vi.spyOn(console, 'warn').mockImplementation(() => undefined);
        const firstDeferred = createDeferred<number>();
        const secondDeferred = createDeferred<number>();
        const commit = vi.fn();
        const {runLatest} = useLatestRequest();
        let firstSignal: AbortSignal | undefined;

        const firstRun = runLatest((signal) => {
            firstSignal = signal;
            return firstDeferred.promise;
        }, commit);
        const secondRun = runLatest(() => secondDeferred.promise, commit);

        expect(firstSignal?.aborted).toBe(true);

        secondDeferred.resolve(2);
        await expect(secondRun).resolves.toBe(true);

        firstDeferred.resolve(1);
        await expect(firstRun).resolves.toBe(false);
        expect(commit).toHaveBeenCalledOnce();
        expect(commit).toHaveBeenCalledWith(2);
    });

    it('invalidates an outstanding request without committing its eventual result', async () => {
        vi.spyOn(console, 'warn').mockImplementation(() => undefined);
        const deferred = createDeferred<number>();
        const commit = vi.fn();
        const {invalidate, runLatest} = useLatestRequest();
        let signal: AbortSignal | undefined;

        const pendingRun = runLatest((requestSignal) => {
            signal = requestSignal;
            return deferred.promise;
        }, commit);

        invalidate();
        expect(signal?.aborted).toBe(true);

        deferred.resolve(1);
        await expect(pendingRun).resolves.toBe(false);
        expect(commit).not.toHaveBeenCalled();
    });
});
