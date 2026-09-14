import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'

const http = vi.hoisted(() => ({
  get: vi.fn(),
  put: vi.fn()
}))

vi.mock('@/axios', () => ({
  default: http
}))

import { useAiPermissionStore } from './AiPermissions'

const enabledReadOnly = {
  canReadAllNotes: true,
  canWriteNoteContent: false,
  canWriteNoteTitle: false,
  canWriteNoteSummary: false,
  canWriteNoteCover: false
}

describe('useAiPermissionStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    http.get.mockReset()
    http.put.mockReset()
  })

  it('starts with the safe user-wide defaults', () => {
    const store = useAiPermissionStore()

    expect(store.permissions).toEqual(enabledReadOnly)
    expect(store.loaded).toBe(false)
  })

  it('loads the complete permission object from the current-user endpoint', async () => {
    const store = useAiPermissionStore()
    http.get.mockResolvedValue({
      data: {
        data: {
          ...enabledReadOnly,
          canWriteNoteContent: true
        }
      }
    })

    await store.loadPermissions()

    expect(http.get).toHaveBeenCalledWith('user/ai/permissions')
    expect(store.permissions.canWriteNoteContent).toBe(true)
    expect(store.loaded).toBe(true)
  })

  it('uses the saved server response after updating all user-wide switches', async () => {
    const store = useAiPermissionStore()
    const next = {...enabledReadOnly, canWriteNoteTitle: true}
    http.put.mockResolvedValue({data: {data: next}})

    await store.updatePermissions(next)

    expect(http.put).toHaveBeenCalledWith('user/ai/permissions', next)
    expect(store.permissions).toEqual(next)
    expect(store.saving).toBe(false)
  })

  it('rolls switches back when saving fails', async () => {
    const store = useAiPermissionStore()
    http.put.mockRejectedValue(new Error('network failed'))

    await expect(store.updatePermissions({...enabledReadOnly, canWriteNoteCover: true})).rejects.toThrow('network failed')

    expect(store.permissions).toEqual(enabledReadOnly)
    expect(store.saving).toBe(false)
  })

  it('clears cached permissions at the account boundary', async () => {
    const store = useAiPermissionStore()
    http.get.mockResolvedValue({data: {data: {...enabledReadOnly, canWriteNoteSummary: true}}})
    await store.loadPermissions()

    store.resetPermissions()

    expect(store.permissions).toEqual(enabledReadOnly)
    expect(store.loaded).toBe(false)
  })
})
