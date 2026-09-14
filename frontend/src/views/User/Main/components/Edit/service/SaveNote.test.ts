import {beforeEach, describe, expect, it, vi} from 'vitest'
import {createPinia, setActivePinia} from 'pinia'

const http = vi.hoisted(() => ({
  post: vi.fn(),
}))
const messages = vi.hoisted(() => ({
  success: vi.fn(),
  warning: vi.fn(),
  error: vi.fn(),
}))

vi.mock('../../../../../../axios', () => ({
  default: http,
}))
vi.mock('element-plus', () => ({
  ElMessage: messages,
}))

import {SaveNote} from './SaveNote'
import {useCurrentNoteInfoStore} from '../Pinia/currentNoteInfo'
import {useSaveNoteState} from '../Pinia/SaveNoteState'

describe('SaveNote', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    http.post.mockReset()
    Object.values(messages).forEach(message => message.mockReset())
  })

  it('supports a silent successful save for the AI preflight path', async () => {
    const currentNote = useCurrentNoteInfoStore()
    const saveState = useSaveNoteState()
    currentNote.setNoteInfo(6, 'AI context', [], '', null)
    saveState.updateContent()
    http.post.mockResolvedValue({data: {status: 200}})
    const editor = {getJSON: () => ({type: 'doc', content: []})}

    await expect(SaveNote(editor as any, {silent: true})).resolves.toBe(true)

    expect(http.post).toHaveBeenCalledWith('user/note/SaveNote', {
      noteId: 6,
      content: '{"type":"doc","content":[]}',
    })
    expect(saveState.isSaved).toBe(true)
    expect(messages.success).not.toHaveBeenCalled()
  })
})
