import { effectScope } from 'vue'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { useTodoState } from '@/views/User/Main/components/TodoList/Pinia/TodoState'
import {
  fetchTodoView,
  useTodoMain,
  type Todo,
  type TodoMainFeedback,
  type TodoMainServices
} from './useTodoMain'

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    warning: vi.fn(),
    error: vi.fn(),
    info: vi.fn()
  },
  ElMessageBox: {
    confirm: vi.fn(async () => 'confirm')
  }
}))

interface Deferred<T> {
  promise: Promise<T>
  resolve: (value: T) => void
}

const createDeferred = <T>(): Deferred<T> => {
  let resolve!: (value: T) => void
  const promise = new Promise<T>(promiseResolve => {
    resolve = promiseResolve
  })
  return { promise, resolve }
}

const createTodo = (id: number, status = 0, overrides: Partial<Todo> = {}): Todo => ({
  todo_id: id,
  title: `Todo ${id}`,
  description: '',
  startDate: '2026-07-19T00:00:00Z',
  dueDate: null,
  status,
  category_id: null,
  category_name: null,
  category_type: null,
  todoInfoisDeleted: false,
  ...overrides
})

const createServices = (overrides: Partial<TodoMainServices> = {}): TodoMainServices => ({
  fetchAll: vi.fn(async () => []),
  fetchByDate: vi.fn(async () => []),
  fetchByCategory: vi.fn(async () => []),
  fetchUncategorized: vi.fn(async () => []),
  fetchCompleted: vi.fn(async () => []),
  fetchExpired: vi.fn(async () => []),
  fetchRecycleBin: vi.fn(async () => []),
  fetchWeek: vi.fn(async () => []),
  fetchCategories: vi.fn(async () => []),
  reopenTodo: vi.fn(async () => 200),
  completeTodo: vi.fn(async () => 200),
  addTodo: vi.fn(async () => 200),
  updateTodo: vi.fn(async () => 200),
  deleteTodo: vi.fn(async () => 200),
  getTodayLabel: () => '2026-07-19',
  now: () => new Date(2026, 6, 19),
  ...overrides
})

const createFeedback = (): TodoMainFeedback => ({
  success: vi.fn(),
  warning: vi.fn(),
  error: vi.fn(),
  info: vi.fn(),
  confirmDelete: vi.fn(async () => true)
})

describe('useTodoMain', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('resolves the today view with a stable local-date API parameter', async () => {
    const fetchByDate = vi.fn(async () => [createTodo(1)])
    const services = createServices({ fetchByDate })

    const result = await fetchTodoView({ state: 1, selectedClass: null }, services)

    expect(fetchByDate).toHaveBeenCalledWith('2026-07-19')
    expect(result).toEqual({
      title: '今天待办(2026-07-19)',
      data: [createTodo(1)]
    })
  })

  it('only commits the newest query response', async () => {
    const first = createDeferred<Todo[]>()
    const second = createDeferred<Todo[]>()
    const services = createServices({
      fetchAll: vi.fn(() => first.promise),
      fetchCompleted: vi.fn(() => second.promise)
    })
    const scope = effectScope()
    const main = scope.run(() => useTodoMain({
      services,
      feedback: createFeedback(),
      registerLifecycle: false
    }))!

    const firstRequest = main.refreshTodoList()
    useTodoState().ToCompletedView()
    const secondRequest = main.refreshTodoList()

    second.resolve([createTodo(2, 1)])
    await expect(secondRequest).resolves.toBe(true)
    first.resolve([createTodo(1)])
    await expect(firstRequest).resolves.toBe(false)

    expect(main.pageTitle.value).toBe('已完成')
    expect(main.TodoData.value.map(todo => todo.todo_id)).toEqual([2])
    scope.stop()
  })

  it('invalidates a deactivated request and refreshes the current view on reactivation', async () => {
    const pending = createDeferred<Todo[]>()
    const fetchAll = vi.fn()
      .mockReturnValueOnce(pending.promise)
      .mockResolvedValueOnce([createTodo(2)])
    const scope = effectScope()
    const main = scope.run(() => useTodoMain({
      services: createServices({ fetchAll }),
      feedback: createFeedback(),
      registerLifecycle: false
    }))!

    const initialRequest = main.refreshTodoList()
    main.deactivateTodoList()
    pending.resolve([createTodo(1)])
    await expect(initialRequest).resolves.toBe(false)
    expect(main.TodoData.value).toEqual([])

    await main.activateTodoList()
    expect(main.TodoData.value.map(todo => todo.todo_id)).toEqual([2])
    expect(fetchAll).toHaveBeenCalledTimes(2)
    scope.stop()
  })

  it('paginates before splitting status groups', async () => {
    const todos = Array.from({ length: 120 }, (_, index) => createTodo(index + 1, index % 2))
    const scope = effectScope()
    const main = scope.run(() => useTodoMain({
      services: createServices({ fetchAll: vi.fn(async () => todos) }),
      feedback: createFeedback(),
      registerLifecycle: false
    }))!

    await main.refreshTodoList()
    main.currentPage.value = 2

    expect(main.pagedTodos.value.map(todo => todo.todo_id)).toEqual(
      Array.from({ length: 50 }, (_, index) => index + 51)
    )
    expect(main.inProgressTodos.value).toHaveLength(25)
    expect(main.completedTodos.value).toHaveLength(25)
    scope.stop()
  })

  it('keeps empty form values undefined in the UI, submits null and refreshes the active filter', async () => {
    const fetchCompleted = vi.fn(async () => [createTodo(9, 1)])
    const addTodo = vi.fn(async () => 200)
    const updateTodo = vi.fn(async () => 200)
    const completeTodo = vi.fn(async () => 200)
    const reopenTodo = vi.fn(async () => 200)
    const deleteTodo = vi.fn(async () => 200)
    const services = createServices({
      fetchCompleted,
      addTodo,
      updateTodo,
      completeTodo,
      reopenTodo,
      deleteTodo
    })
    const feedback = createFeedback()
    useTodoState().ToCompletedView()
    const scope = effectScope()
    const main = scope.run(() => useTodoMain({
      services,
      feedback,
      registerLifecycle: false
    }))!

    main.newTodoForm.value = {
      title: 'Advanced',
      description: '',
      categoryId: undefined,
      dueDate: undefined
    }
    await main.submitAddTodo()
    expect(addTodo).toHaveBeenLastCalledWith({
      title: 'Advanced',
      description: '',
      categoryId: null,
      dueDate: null
    })
    expect(main.newTodoForm.value.categoryId).toBeUndefined()
    expect(main.newTodoForm.value.dueDate).toBeUndefined()

    main.newTodoInput.value = ' Quick '
    await main.handleAddTodoInput({ key: 'Enter' } as KeyboardEvent)
    expect(addTodo).toHaveBeenLastCalledWith({
      title: 'Quick',
      description: '',
      categoryId: null,
      dueDate: null
    })

    const todo = createTodo(3, 0, { category_id: null, dueDate: null })
    main.openTodoDetail(todo, true)
    expect(main.currentTodo.value.category_id).toBeUndefined()
    expect(main.currentTodo.value.dueDate).toBeUndefined()
    await main.handleUpdateTodo()
    expect(updateTodo).toHaveBeenCalledWith(3, {
      title: 'Todo 3',
      description: '',
      categoryId: null,
      dueDate: null,
      status: 0
    })

    await main.CompleteTodoProxy(3)
    await main.ReopenTodoProxy(3)
    await main.handleDeleteTodo(todo)

    expect(completeTodo).toHaveBeenCalledWith(3)
    expect(reopenTodo).toHaveBeenCalledWith(3)
    expect(deleteTodo).toHaveBeenCalledWith(3)
    expect(fetchCompleted).toHaveBeenCalledTimes(6)
    scope.stop()
  })
})
