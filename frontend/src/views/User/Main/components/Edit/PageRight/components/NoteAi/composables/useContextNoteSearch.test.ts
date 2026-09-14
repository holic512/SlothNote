import { effectScope, nextTick } from 'vue'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { ContextNote } from '../service/AiChat'
import { searchContextNotes } from '../service/searchContextNotes'
import { useContextNoteSearch } from './useContextNoteSearch'

vi.mock('../service/searchContextNotes', () => ({
  searchContextNotes: vi.fn()
}))

const mockedSearchContextNotes = vi.mocked(searchContextNotes)

describe('useContextNoteSearch', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    mockedSearchContextNotes.mockReset()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('debounces searches, aborts stale requests and only commits the newest result', async () => {
    let resolveFirst!: (notes: ContextNote[]) => void
    let resolveSecond!: (notes: ContextNote[]) => void
    let firstSignal: AbortSignal | undefined

    mockedSearchContextNotes.mockImplementation((keyword, signal) => new Promise(resolve => {
      if (keyword === 'first') {
        firstSignal = signal
        resolveFirst = resolve
        return
      }
      resolveSecond = resolve
    }))

    const scope = effectScope()
    const search = scope.run(() => useContextNoteSearch())!
    search.noteSearchVisible.value = true
    search.noteKeyword.value = 'first'
    await nextTick()
    await vi.advanceTimersByTimeAsync(320)
    expect(mockedSearchContextNotes).toHaveBeenCalledTimes(1)

    search.noteKeyword.value = 'second'
    await nextTick()
    expect(firstSignal?.aborted).toBe(true)
    await vi.advanceTimersByTimeAsync(320)

    resolveSecond([{ noteId: 2, title: 'Second' }])
    await Promise.resolve()
    await nextTick()
    expect(search.noteSearchResults.value).toEqual([{ noteId: 2, title: 'Second' }])

    resolveFirst([{ noteId: 1, title: 'First' }])
    await Promise.resolve()
    await nextTick()
    expect(search.noteSearchResults.value).toEqual([{ noteId: 2, title: 'Second' }])
    expect(search.noteSearching.value).toBe(false)
    scope.stop()
  })

  it('cancels a pending debounce when the selector closes', async () => {
    const scope = effectScope()
    const search = scope.run(() => useContextNoteSearch())!
    search.noteSearchVisible.value = true
    search.noteKeyword.value = 'pending'
    await nextTick()

    search.noteSearchVisible.value = false
    await nextTick()
    await vi.advanceTimersByTimeAsync(320)

    expect(mockedSearchContextNotes).not.toHaveBeenCalled()
    expect(search.noteSearching.value).toBe(false)
    scope.stop()
  })
})
