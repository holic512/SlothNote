import {beforeEach, describe, expect, it} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useTodoState} from './TodoState';

describe('TodoState', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it('tracks the active query view and increments revision for each query change', () => {
        const store = useTodoState();

        expect(store.$state).toMatchObject({
            state: 0,
            AClass: null,
            customViewData: null,
            revision: 0,
        });

        store.ToClass({id: 7, name: 'Work', type: 2});
        expect(store.$state).toMatchObject({
            state: 2,
            AClass: {id: 7, name: 'Work', type: 2},
            customViewData: null,
            revision: 1,
        });

        store.refresh();
        expect(store.revision).toBe(2);
        expect(store.state).toBe(2);
        expect(store.AClass?.id).toBe(7);

        store.ToCompletedView();
        expect(store.$state).toMatchObject({
            state: 4,
            AClass: null,
            customViewData: null,
            revision: 3,
        });

        const customQuery = {status: 'pending', owner: 'me'};
        store.ToCustomView(customQuery);
        expect(store.$state).toMatchObject({
            state: 7,
            AClass: null,
            customViewData: customQuery,
            revision: 4,
        });
    });

    it('resets both the query and refresh revision at the account boundary', () => {
        const store = useTodoState();
        store.ToExpiredView();
        store.refresh();

        store.$reset();

        expect(store.$state).toEqual({
            state: 0,
            AClass: null,
            customViewData: null,
            revision: 0,
        });
    });
});
