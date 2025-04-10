// stores/useSaveNoteState.ts
import {defineStore} from 'pinia'
import {ref, Ref} from 'vue'

export const useSaveNoteState = defineStore('SaveNoteState', {
    // Define state variables
    state() {
        return {
            isSaved: ref(true) as Ref<boolean>,
            // Whether content is saved
        }
    },

    // Define actions (methods)
    actions: {

        // 未保存
        updateContent(): void {
            this.isSaved = false
        },

        // 已保存
        saveContent(): void {
            if (!this.isSaved) {
                this.isSaved = true
            }
        }
    },

})
