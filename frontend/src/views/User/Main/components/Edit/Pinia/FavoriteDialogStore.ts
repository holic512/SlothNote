import {defineStore} from 'pinia';

export const useFavoriteDialogStore = defineStore('favoriteDialog', {
  state: () => ({
    visible: false,
    noteId: null as number | null,
  }),
  actions: {
    open(noteId: number) {
      this.noteId = noteId;
      this.visible = true;
    },
    close() {
      this.visible = false;
      this.noteId = null;
    }
  },
  persist: false,
});