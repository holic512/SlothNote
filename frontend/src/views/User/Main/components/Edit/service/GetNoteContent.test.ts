import {beforeEach, describe, expect, it, vi} from 'vitest'

const http = vi.hoisted(() => ({
  get: vi.fn(),
}))

vi.mock('../../../../../../axios', () => ({
  default: http,
}))

import {getNoteContent} from './GetNoteContent'

describe('getNoteContent', () => {
  beforeEach(() => {
    http.get.mockReset()
  })

  it('reads the latest note response instead of allowing a cached body', async () => {
    http.get.mockResolvedValue({
      data: {
        status: 200,
        data: {noteId: 17, content: '{"type":"doc"}'},
      },
    })

    await expect(getNoteContent(17)).resolves.toEqual({noteId: 17, content: '{"type":"doc"}'})
    expect(http.get).toHaveBeenCalledWith('user/note/context', expect.objectContaining({
      params: expect.objectContaining({noteId: 17, _fresh: expect.any(Number)}),
      headers: {'Cache-Control': 'no-cache', Pragma: 'no-cache'},
    }))
  })

  it('does not treat a rejected API payload as note content', async () => {
    http.get.mockResolvedValue({data: {status: 403, data: {content: 'stale'}}})

    await expect(getNoteContent(17)).resolves.toBeNull()
  })
})
