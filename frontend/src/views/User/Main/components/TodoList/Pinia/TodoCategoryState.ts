import {defineStore} from 'pinia';

interface State {
    revision: number
}

export const useTodoCategoryState = defineStore('TodoCategoryState', {
    state(): State {
        return {
            revision: 0,
        }
    },
    actions: {
        description() {
            this.revision += 1;
        },

    },

});
