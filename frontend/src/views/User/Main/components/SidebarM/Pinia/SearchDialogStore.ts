import {defineStore} from 'pinia';

export const useSearchDialogStore = defineStore('SearchDialogStore', {
  state: () => ({
    visible: false,
  }),
  actions: {
    open() { this.visible = true; },
    close() { this.visible = false; },
  },
});