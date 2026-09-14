/**
 * @file useContextNoteSearch
 * @project SlothNote
 * @module 用户端 / 笔记 AI 上下文搜索
 * @description 管理 AI 上下文笔记搜索弹窗、输入防抖、请求取消和过期响应隔离。
 * @logic 1. 关键词变化后延迟搜索；2. 新搜索取消旧请求；3. 仅当前请求可提交结果和加载状态；4. 关闭或卸载时释放任务。
 * @dependencies Vue, Service: searchContextNotes, Types: AiChat
 * @index_tags AI 上下文, 笔记搜索, 防抖, 请求竞态
 * @author holic512
 */
import { onScopeDispose, ref, watch } from 'vue'
import type { ContextNote } from '../service/AiChat'
import { searchContextNotes } from '../service/searchContextNotes'

const DEFAULT_DEBOUNCE_MS = 320

export const useContextNoteSearch = (debounceMs = DEFAULT_DEBOUNCE_MS) => {
  const noteSearchVisible = ref(false)
  const noteKeyword = ref('')
  const noteSearching = ref(false)
  const noteSearchResults = ref<ContextNote[]>([])

  let searchTimer: ReturnType<typeof setTimeout> | undefined
  let searchController: AbortController | undefined
  let searchRequestId = 0

  const cancelPendingSearch = () => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = undefined
    }
    searchController?.abort()
    searchController = undefined
  }

  const loadSearchResults = async (keyword: string, requestId: number) => {
    const controller = new AbortController()
    searchController = controller
    try {
      const result = await searchContextNotes(keyword, controller.signal)
      if (controller.signal.aborted || requestId !== searchRequestId) return
      noteSearchResults.value = result
    } catch (error) {
      if (!controller.signal.aborted && requestId === searchRequestId) {
        console.warn('[NoteAI] context note search failed', error)
        noteSearchResults.value = []
      }
    } finally {
      if (requestId === searchRequestId) {
        noteSearching.value = false
        searchController = undefined
      }
    }
  }

  const scheduleSearch = (keyword: string) => {
    cancelPendingSearch()
    const requestId = ++searchRequestId
    const normalizedKeyword = keyword.trim()
    noteSearchResults.value = []

    if (!normalizedKeyword) {
      noteSearching.value = false
      return
    }

    noteSearching.value = true
    searchTimer = setTimeout(() => {
      searchTimer = undefined
      void loadSearchResults(normalizedKeyword, requestId)
    }, debounceMs)
  }

  const invalidateSearch = () => {
    searchRequestId += 1
    cancelPendingSearch()
    noteSearching.value = false
  }

  const openNoteSearch = () => {
    invalidateSearch()
    noteKeyword.value = ''
    noteSearchResults.value = []
    noteSearchVisible.value = true
  }

  watch(noteKeyword, scheduleSearch)
  watch(noteSearchVisible, visible => {
    if (!visible) {
      invalidateSearch()
    }
  })

  onScopeDispose(invalidateSearch)

  return {
    noteSearchVisible,
    noteKeyword,
    noteSearching,
    noteSearchResults,
    openNoteSearch
  }
}
