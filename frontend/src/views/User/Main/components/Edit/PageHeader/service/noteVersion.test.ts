import {beforeEach, describe, expect, it, vi} from 'vitest'

const http = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
}))

vi.mock('@/axios', () => ({
  default: http,
}))

import {fetchNoteVersionDetail, fetchNoteVersions, restoreNoteVersion} from './noteVersion'

describe('noteVersion service', () => {
  beforeEach(() => {
    http.get.mockReset()
    http.post.mockReset()
  })

  it('loads the version list and detail with a local no-cache policy', async () => {
    http.get
      .mockResolvedValueOnce({data: {data: [{id: 2, versionNo: 2}]}})
      .mockResolvedValueOnce({data: {status: 200, data: {id: 2, contentJson: '{"type":"doc"}'}}})

    await expect(fetchNoteVersions(7)).resolves.toEqual([{id: 2, versionNo: 2}])
    await expect(fetchNoteVersionDetail(7, 2)).resolves.toEqual({id: 2, contentJson: '{"type":"doc"}'})
    expect(http.get).toHaveBeenNthCalledWith(1, 'user/note/versions', expect.objectContaining({
      params: expect.objectContaining({noteId: 7, _fresh: expect.any(Number)}),
      headers: {'Cache-Control': 'no-cache', Pragma: 'no-cache'},
    }))
    expect(http.get).toHaveBeenNthCalledWith(2, 'user/note/version/detail', expect.objectContaining({
      params: expect.objectContaining({noteId: 7, versionId: 2, _fresh: expect.any(Number)}),
      headers: {'Cache-Control': 'no-cache', Pragma: 'no-cache'},
    }))
  })

  it('returns the server-saved body after a restore request', async () => {
    const restored = {status: 200, data: {noteId: 7, content: '{"type":"doc","content":[]}'}}
    http.post.mockResolvedValue({data: restored})

    await expect(restoreNoteVersion(7, 2)).resolves.toEqual(restored)
    expect(http.post).toHaveBeenCalledWith('user/note/RestoreVersion', {noteId: 7, versionId: 2})
  })
})
