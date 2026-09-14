/**
 * @file useTodoMain
 * @project SlothNote
 * @module 用户端 / 待办主列表编排
 * @description 统一管理待办查询、最新请求提交、KeepAlive 生命周期、分页、分类和增删改表单状态。
 * @logic 1. 按 TodoState 快照选择查询；2. 通过请求序号和激活状态隔离过期响应；3. 所有变更后按当前视图刷新；4. 表单使用 undefined，提交边界转换为 null。
 * @dependencies Vue, Pinia Store: useTodoState, Service: TodoMain/Service, Service: ClassTree
 * @index_tags 待办查询, KeepAlive, 请求竞态, 待办表单, 分页
 * @author holic512
 */
import {
  computed,
  onActivated,
  onBeforeUnmount,
  onDeactivated,
  onMounted,
  ref,
  watch
} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTodoState } from '@/views/User/Main/components/TodoList/Pinia/TodoState'
import { getTodayDate } from '@/views/User/Main/components/TodoList/Service/getTodayDate'
import { addTodo } from '../Service/AddTodo'
import { CompleteTodo } from '../Service/CompleteTodo'
import { deleteTodo } from '../Service/DeleteTodo'
import { getCompletedTodos } from '../Service/GetCompletedTodos'
import { getExpiredTodos } from '../Service/GetExpiredTodos'
import { getRecycleBinTodos } from '../Service/GetRecycleBinTodos'
import { getTodosByDate } from '../Service/GetTodosByDate'
import { getTodosForWeek } from '../Service/GetTodosForWeek'
import { getUncategorizedTodos } from '../Service/GetUncategorizedTodos'
import { getAllTodoList } from '../Service/getAllTodoList'
import { ReopenTodo } from '../Service/ReopenTodo'
import { TodoTypeById } from '../Service/TodoTypeById'
import { updateTodo } from '../Service/UpdateTodo'
import { GetUserTodoClasses } from '../../TodoListTree/ClassTree/Service/GetUserTodoClasses'
import { getUserTodosByCategory } from '../../TodoListTree/ClassTree/Service/GetUserTodosByCategory'

export interface Todo {
  todo_id: number
  title: string
  description: string
  startDate: string
  dueDate?: string | null
  status: number
  category_id?: number | null
  category_name: string | null
  category_type: number | null
  todoInfoisDeleted: boolean
}

export interface TodoCategory {
  id: number
  name: string
}

export interface TodoEditorState extends Omit<Todo, 'category_id' | 'dueDate'> {
  category_id?: number
  dueDate?: string
}

export interface TodoFormState {
  title: string
  description: string
  categoryId?: number
  dueDate?: string
}

export interface TodoSubmitPayload {
  title: string
  description: string
  categoryId: number | null
  dueDate: string | null
  status?: number
}

interface TodoViewSnapshot {
  state: number
  selectedClass: { id: number; name: string } | null
}

export interface TodoMainServices {
  fetchAll: () => Promise<Todo[]>
  fetchByDate: (date: string) => Promise<Todo[]>
  fetchByCategory: (categoryId: number) => Promise<Todo[]>
  fetchUncategorized: () => Promise<Todo[]>
  fetchCompleted: () => Promise<Todo[]>
  fetchExpired: () => Promise<Todo[]>
  fetchRecycleBin: () => Promise<Todo[]>
  fetchWeek: () => Promise<Todo[]>
  fetchCategories: () => Promise<TodoCategory[]>
  reopenTodo: (todoId: number) => Promise<number | undefined>
  completeTodo: (todoId: number) => Promise<number | undefined>
  addTodo: (payload: TodoSubmitPayload) => Promise<number>
  updateTodo: (todoId: number, payload: TodoSubmitPayload) => Promise<number>
  deleteTodo: (todoId: number) => Promise<number>
  getTodayLabel: () => string
  now: () => Date
}

export interface TodoMainFeedback {
  success: (message: string) => void
  warning: (message: string) => void
  error: (message: string) => void
  info: (message: string) => void
  confirmDelete: () => Promise<boolean>
}

interface UseTodoMainOptions {
  services?: TodoMainServices
  feedback?: TodoMainFeedback
  pageSize?: number
  registerLifecycle?: boolean
}

const asTodoList = (value: Todo[] | null | undefined) => value ?? []

const defaultServices: TodoMainServices = {
  fetchAll: async () => asTodoList(await getAllTodoList()),
  fetchByDate: async date => asTodoList(await getTodosByDate(date)),
  fetchByCategory: async categoryId => asTodoList(await getUserTodosByCategory(categoryId)),
  fetchUncategorized: async () => asTodoList(await getUncategorizedTodos()),
  fetchCompleted: async () => asTodoList(await getCompletedTodos()),
  fetchExpired: async () => asTodoList(await getExpiredTodos()),
  fetchRecycleBin: async () => asTodoList(await getRecycleBinTodos()),
  fetchWeek: async () => asTodoList(await getTodosForWeek()),
  fetchCategories: async () => (await GetUserTodoClasses()) ?? [],
  reopenTodo: ReopenTodo,
  completeTodo: CompleteTodo,
  addTodo,
  updateTodo,
  deleteTodo,
  getTodayLabel: getTodayDate,
  now: () => new Date()
}

const defaultFeedback: TodoMainFeedback = {
  success: message => ElMessage.success(message),
  warning: message => ElMessage.warning(message),
  error: message => ElMessage.error(message),
  info: message => ElMessage.info(message),
  confirmDelete: async () => {
    const result = await ElMessageBox.confirm(
      '确定要删除这个待办事项吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).catch(() => false)
    return Boolean(result)
  }
}

export const createEmptyTodoForm = (): TodoFormState => ({
  title: '',
  description: '',
  categoryId: undefined,
  dueDate: undefined
})

export const normalizeTodoForEditor = (todo: Todo): TodoEditorState => ({
  ...todo,
  category_id: todo.category_id ?? undefined,
  dueDate: todo.dueDate ?? undefined
})

export const toTodoSubmitPayload = (
  form: TodoFormState,
  status?: number
): TodoSubmitPayload => ({
  title: form.title,
  description: form.description || '',
  categoryId: form.categoryId ?? null,
  dueDate: form.dueDate ?? null,
  ...(status === undefined ? {} : { status })
})

const toEditorSubmitPayload = (todo: TodoEditorState): TodoSubmitPayload => ({
  title: todo.title,
  description: todo.description || '',
  categoryId: todo.category_id ?? null,
  dueDate: todo.dueDate ?? null,
  status: todo.status
})

export const fetchTodoView = async (
  view: TodoViewSnapshot,
  services: TodoMainServices
): Promise<{ title: string; data: Todo[] }> => {
  switch (view.state) {
    case 0:
      return { title: '全部待办', data: await services.fetchAll() }
    case 1: {
      const today = services.now()
      const formattedDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
      return {
        title: `今天待办(${services.getTodayLabel()})`,
        data: await services.fetchByDate(formattedDate)
      }
    }
    case 2:
      if (!view.selectedClass) {
        return { title: '全部待办', data: [] }
      }
      return {
        title: view.selectedClass.name,
        data: await services.fetchByCategory(view.selectedClass.id)
      }
    case 3:
      return { title: '未分类', data: await services.fetchUncategorized() }
    case 4:
      return { title: '已完成', data: await services.fetchCompleted() }
    case 5:
      return { title: '已过期', data: await services.fetchExpired() }
    case 6:
      return { title: '回收站', data: await services.fetchRecycleBin() }
    case 7:
      return { title: '七日待做', data: await services.fetchWeek() }
    default:
      return { title: '全部待办', data: [] }
  }
}

export const useTodoMain = (options: UseTodoMainOptions = {}) => {
  const services = options.services ?? defaultServices
  const feedback = options.feedback ?? defaultFeedback
  const pageSize = options.pageSize ?? 50
  const todoState = useTodoState()

  const pageTitle = ref('全部待办')
  const TodoData = ref<Todo[]>([])
  const currentPage = ref(1)
  const newTodoInput = ref('')
  const addTodoDialogVisible = ref(false)
  const newTodoForm = ref<TodoFormState>(createEmptyTodoForm())
  const todoCategories = ref<TodoCategory[]>([])
  const todoDetailDialogVisible = ref(false)
  const currentTodo = ref<TodoEditorState>({} as TodoEditorState)
  const isEditing = ref(false)

  let listRequestId = 0
  let isActive = true
  let wasDeactivated = false

  const pagedTodos = computed(() => {
    const start = (currentPage.value - 1) * pageSize
    return TodoData.value.slice(start, start + pageSize)
  })
  const inProgressTodos = computed(() => pagedTodos.value.filter(todo => todo.status === 0))
  const completedTodos = computed(() => pagedTodos.value.filter(todo => todo.status === 1))
  const currentViewState = computed(() => todoState.state)

  const invalidateTodoListRequest = () => {
    listRequestId += 1
  }

  const refreshTodoList = async () => {
    const requestId = ++listRequestId
    const view: TodoViewSnapshot = {
      state: todoState.state,
      selectedClass: todoState.AClass
        ? { id: todoState.AClass.id, name: todoState.AClass.name }
        : null
    }

    try {
      const result = await fetchTodoView(view, services)
      if (requestId !== listRequestId || !isActive) return false

      pageTitle.value = result.title
      TodoData.value = result.data
      currentPage.value = 1
      return true
    } catch (error) {
      if (requestId !== listRequestId || !isActive) return false
      console.error('加载待办列表出错:', error)
      feedback.error('加载待办列表失败')
      return false
    }
  }

  const mountTodoList = async () => {
    isActive = true
    await refreshTodoList()
  }

  const activateTodoList = async () => {
    isActive = true
    if (!wasDeactivated) return
    wasDeactivated = false
    await refreshTodoList()
  }

  const deactivateTodoList = () => {
    isActive = false
    wasDeactivated = true
    invalidateTodoListRequest()
  }

  const disposeTodoList = () => {
    isActive = false
    invalidateTodoListRequest()
  }

  const ReopenTodoProxy = async (todoId: number) => {
    const status = await services.reopenTodo(todoId)
    if (status === 200) {
      await refreshTodoList()
    } else {
      feedback.info('无法连接到服务器')
    }
  }

  const CompleteTodoProxy = async (todoId: number) => {
    const status = await services.completeTodo(todoId)
    if (status === 200) {
      await refreshTodoList()
    } else {
      feedback.info('无法连接到服务器')
    }
  }

  const handleAddTodoInput = async (event: KeyboardEvent) => {
    const title = newTodoInput.value.trim()
    if (event.key !== 'Enter' || !title) return

    const status = await services.addTodo({
      title,
      description: '',
      categoryId: null,
      dueDate: null
    })
    if (status === 200) {
      feedback.success('添加成功')
      await refreshTodoList()
      newTodoInput.value = ''
    } else {
      feedback.error('添加失败')
    }
  }

  const fetchCategories = async () => {
    todoCategories.value = await services.fetchCategories()
  }

  const openAddTodoForm = () => {
    void fetchCategories()
    addTodoDialogVisible.value = true
  }

  const submitAddTodo = async () => {
    if (!newTodoForm.value.title) {
      feedback.warning('标题不能为空')
      return
    }

    const status = await services.addTodo(toTodoSubmitPayload(newTodoForm.value))
    if (status === 200) {
      feedback.success('添加成功')
      await refreshTodoList()
      addTodoDialogVisible.value = false
      newTodoForm.value = createEmptyTodoForm()
    } else {
      feedback.error('添加失败')
    }
  }

  const openTodoDetail = (todo: Todo, edit = false) => {
    currentTodo.value = normalizeTodoForEditor(todo)
    todoDetailDialogVisible.value = true
    isEditing.value = edit
    void fetchCategories()
  }

  const handleUpdateTodo = async () => {
    if (!currentTodo.value.title) {
      feedback.warning('标题不能为空')
      return
    }

    const status = await services.updateTodo(
      currentTodo.value.todo_id,
      toEditorSubmitPayload(currentTodo.value)
    )
    if (status === 200) {
      feedback.success('更新成功')
      await refreshTodoList()
      isEditing.value = false
      todoDetailDialogVisible.value = false
    } else {
      feedback.error('更新失败')
    }
  }

  const handleDeleteTodo = async (todo?: Todo) => {
    if (todo) {
      currentTodo.value = normalizeTodoForEditor(todo)
    }
    if (!await feedback.confirmDelete()) return

    const status = await services.deleteTodo(currentTodo.value.todo_id)
    if (status === 200) {
      feedback.success('删除成功')
      await refreshTodoList()
      todoDetailDialogVisible.value = false
    } else {
      feedback.error('删除失败')
    }
  }

  if (options.registerLifecycle !== false) {
    watch(() => todoState.revision, () => {
      if (isActive) void refreshTodoList()
    })
    onMounted(() => void mountTodoList())
    onActivated(() => void activateTodoList())
    onDeactivated(deactivateTodoList)
    onBeforeUnmount(disposeTodoList)
  }

  return {
    currentViewState,
    pageTitle,
    TodoData,
    currentPage,
    pageSize,
    pagedTodos,
    inProgressTodos,
    completedTodos,
    newTodoInput,
    addTodoDialogVisible,
    newTodoForm,
    todoCategories,
    todoDetailDialogVisible,
    currentTodo,
    isEditing,
    refreshTodoList,
    mountTodoList,
    activateTodoList,
    deactivateTodoList,
    disposeTodoList,
    ReopenTodoProxy,
    CompleteTodoProxy,
    handleAddTodoInput,
    fetchCategories,
    openAddTodoForm,
    submitAddTodo,
    openTodoDetail,
    handleUpdateTodo,
    handleDeleteTodo,
    resolveTodoTagType: (categoryType: number | null) => TodoTypeById(categoryType ?? 0)
  }
}
