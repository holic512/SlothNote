import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {UseUpdateCommentState} from './UpdateCommentState';

describe('CommentUpdateState', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('publishes every comment mutation as a distinct revision', () => {
        const store = UseUpdateCommentState();

        store.needUpdate();
        store.needUpdate();

        expect(store.revision).toBe(2);
    });

    it('resets the revision at the user session boundary', () => {
        const store = UseUpdateCommentState();
        store.needUpdate();

        store.$reset();

        expect(store.revision).toBe(0);
    });
});
