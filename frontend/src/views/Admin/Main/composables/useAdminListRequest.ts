/**
 * @file useAdminListRequest
 * @project SlothNote
 * @module 管理后台 / 列表请求控制
 * @description 提供管理端列表页统一的最大页数计算、旧请求取消与“仅最新请求可提交”并发保护。
 * @logic 1. 将总数与分页大小归一化为至少一页；2. 新请求启动时中止旧请求并分配递增序号；3. 组件卸载、过期或失败的请求均不提交且不向页面生命周期抛错。
 * @dependencies Vue: onBeforeUnmount
 * @index_tags 管理端列表, 分页状态, 请求竞态, 异步并发控制
 * @author holic512
 */
import { onBeforeUnmount } from 'vue'

export const getMaxPage = (total: number, pageSize: number): number => {
  return Math.max(1, Math.ceil(total / Math.max(1, pageSize)))
}

export const useLatestRequest = () => {
  let requestSequence = 0
  let activeController: AbortController | null = null

  const invalidate = () => {
    requestSequence += 1
    activeController?.abort()
    activeController = null
  }

  const runLatest = async <T>(
    request: (signal: AbortSignal) => Promise<T>,
    commit: (result: T) => void,
  ): Promise<boolean> => {
    activeController?.abort()
    const controller = new AbortController()
    activeController = controller
    const currentSequence = ++requestSequence

    try {
      const result = await request(controller.signal)
      if (controller.signal.aborted || currentSequence !== requestSequence) {
        return false
      }
      commit(result)
      return true
    } catch {
      if (controller.signal.aborted || currentSequence !== requestSequence) {
        return false
      }
      return false
    } finally {
      if (activeController === controller) {
        activeController = null
      }
    }
  }

  onBeforeUnmount(invalidate)

  return {
    invalidate,
    runLatest,
  }
}
