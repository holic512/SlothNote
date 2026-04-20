# Codex 项目规则

本文件用于帮助 Codex 在 `SlothNote` 项目中快速定位核心文件，减少无效搜索。修改代码前，优先按下面的“功能落点规则”定位，而不是全仓库盲搜。

## 1. 项目总览

- 仓库是前后端分离结构：
  - `frontend/`：Vue 3 + TypeScript + Vite
  - `backend/`：Spring Boot 3 + JPA + Sa-Token
- 根目录说明类文件：
  - `README.md`
  - `README-en.md`

## 2. 优先入口文件

先看这些文件，可以最快判断功能挂载位置：

- 前端应用入口：`frontend/src/main.ts`
- 前端总路由：`frontend/src/router/index.js`
- 首页路由：`frontend/src/views/Home/router.ts`
- 用户端路由：`frontend/src/views/User/router.ts`
- 管理端路由：`frontend/src/views/Admin/router.ts`
- Axios 实例与鉴权注入：`frontend/src/axios/index.ts`
- 后端启动入口：`backend/src/main/java/org/example/backend/BackendApplication.java`
- 后端全局配置：`backend/src/main/resources/application.yml`
- 初始化 SQL：`backend/src/main/resources/sql/init.sql`

## 3. 前端核心目录规则

### 3.1 页面入口

- 首页：`frontend/src/views/Home/`
- 用户端：`frontend/src/views/User/`
- 管理端：`frontend/src/views/Admin/`

### 3.2 用户端页面定位

- 登录/注册：`frontend/src/views/User/Auth/`
- 用户主框架：`frontend/src/views/User/Main/index.vue`
- 用户首页：`frontend/src/views/User/Main/components/Home/`
- 笔记编辑页：`frontend/src/views/User/Main/components/Edit/`
- 待办页：`frontend/src/views/User/Main/components/TodoList/`
- 收藏页：`frontend/src/views/User/Main/components/MyStar/`
- 设置页：`frontend/src/views/User/Main/components/Setting/`
- 用户初始化引导：`frontend/src/views/User/Main/components/UserProfileInit/`

### 3.3 笔记相关前端定位

用户笔记功能主要分散在以下目录：

- 笔记树与侧边栏：`frontend/src/views/User/Main/components/Sidebar/`
- 笔记树数据接口：`frontend/src/views/User/Main/components/Sidebar/NoteTree/service/`
- 右键菜单操作：`frontend/src/views/User/Main/components/Sidebar/RightMenu/Service/`
- 重命名相关：`frontend/src/views/User/Main/components/Sidebar/components/Rename/Service/`
- 描述/摘要相关：`frontend/src/views/User/Main/components/Sidebar/components/Description/Service/`
- 详情面板相关：`frontend/src/views/User/Main/components/Sidebar/components/Details/Service/`
- 编辑器主体：`frontend/src/views/User/Main/components/Edit/Main/`
- 编辑器工具栏：`frontend/src/views/User/Main/components/Edit/Main/Tools/`
- 气泡菜单：`frontend/src/views/User/Main/components/Edit/Main/BubbleMenu/`
- 笔记保存与内容读取：`frontend/src/views/User/Main/components/Edit/service/`
- 封面设置：`frontend/src/views/User/Main/components/Edit/Main/SetCover/`
- AI 面板：`frontend/src/views/User/Main/components/Edit/PageRight/components/NoteAi/`
- 评论面板：`frontend/src/views/User/Main/components/Edit/PageRight/components/NoteComment/`

### 3.4 待办相关前端定位

- 待办页外层：`frontend/src/views/User/Main/components/TodoList/`
- 待办主列表接口：`frontend/src/views/User/Main/components/TodoList/TodoMain/Service/`
- 待办分类树接口：`frontend/src/views/User/Main/components/TodoList/TodoListTree/ClassTree/Service/`

### 3.5 管理端页面定位

管理端功能基本按 `*Mm` 模块分组，页面都在：

- `frontend/src/views/Admin/Main/view/`

对应页面目录：

- 仪表盘：`DashboardMm/`
- 用户管理：`userMm/`
- 评论管理：`CommentMm/`
- 笔记管理：`NoteMm/`
- 文件夹管理：`FolderMm/`
- 待办管理：`TodoMm/`
- 收藏管理：`FavoriteMm/`
- 设置页：`Setting/`

规则：如果是管理端某个功能，先去 `frontend/src/views/Admin/Main/view/<模块名>/` 找页面，再看其 `components/` 下的请求文件。

## 4. 后端核心目录规则

### 4.1 公共层

- 通用鉴权：`backend/src/main/java/org/example/backend/common/Auth/`
- 全局异常：`backend/src/main/java/org/example/backend/common/config/Exception/`
- Redis 配置：`backend/src/main/java/org/example/backend/common/config/Redis/`
- Sa-Token 配置：`backend/src/main/java/org/example/backend/common/config/SaToken/`
- 实体类：`backend/src/main/java/org/example/backend/common/entity/`
- 公共 Repository：`backend/src/main/java/org/example/backend/common/repository/`
- 公共响应包装：`backend/src/main/java/org/example/backend/common/response/`
- 邮件与 MQ：`backend/src/main/java/org/example/backend/common/Mail/`、`backend/src/main/java/org/example/backend/common/rabbitMQ/`
- 文件存储：`backend/src/main/java/org/example/backend/common/util/file/`

### 4.2 用户端后端定位

- 用户鉴权：`backend/src/main/java/org/example/backend/user/auth/`
- 用户仪表盘：`backend/src/main/java/org/example/backend/user/dashboard/`
- 用户设置：`backend/src/main/java/org/example/backend/user/settings/account/`
- 用户 AI：`backend/src/main/java/org/example/backend/user/ai/`
- 用户待办：`backend/src/main/java/org/example/backend/user/todo/`
- 用户笔记总模块：`backend/src/main/java/org/example/backend/user/note/`

用户笔记模块继续细分为：

- 笔记内容：`user/note/note/`
- 笔记树：`user/note/noteTree/`
- 封面：`user/note/noteCover/`
- 图片：`user/note/noteImage/`
- 搜索：`user/note/search/`
- 收藏：`user/note/favoriteNote/`
- 评论：`user/note/comment/`

### 4.3 管理端后端定位

管理端功能都在：

- `backend/src/main/java/org/example/backend/admin/`

模块分布：

- 登录鉴权：`admin/auth/`
- 仪表盘：`admin/dashboard/`
- 用户管理：`admin/userMm/`
- 评论管理：`admin/commentMm/`
- 笔记管理：`admin/noteMm/`
- 文件夹管理：`admin/folderMm/`
- 待办管理：`admin/todoMm/`
- 收藏管理：`admin/favoriteMm/`
- 管理端公共 Repository：`admin/repository/`

## 5. 后端分层查找规则

后端大多数模块遵循以下查找顺序：

1. 先找 `controller` 或 `Controller`
2. 再找 `service`
3. 再找 `service/impl`
4. 再找 `repository`
5. 参数对象一般在 `dto`、`request`、`pojo`
6. 状态值与分支常量一般在 `enums`

注意：本项目命名大小写不完全统一，存在以下情况，定位时不要擅自假设目录名统一：

- 有的目录叫 `controller`，有的叫 `Controller`
- 有的实现目录叫 `impl`，有的叫 `Impl`
- 接口路径大小写也不完全统一，例如：
  - `user/note/SaveNote`
  - `user/noteTree/NoteAvatar`
  - `user/auth/PLogin`

规则：查接口时，优先按已有大小写精确搜索，不要先做命名规范化重构。

## 6. 按功能快速定位

### 6.1 用户登录/注册

- 前端页面：`frontend/src/views/User/Auth/`
- 前端请求：`frontend/src/views/User/Auth/login/service/`、`frontend/src/views/User/Auth/register/services/`
- 后端控制器：`backend/src/main/java/org/example/backend/user/auth/controller/UserAuthController.java`
- 后端服务：`backend/src/main/java/org/example/backend/user/auth/service/`

### 6.2 用户设置

- 前端页面：`frontend/src/views/User/Main/components/Setting/`
- 前端请求：`frontend/src/views/User/Main/components/Setting/service/`
- 后端控制器：`backend/src/main/java/org/example/backend/user/settings/account/controller/AccountSettingsController.java`

### 6.3 笔记树、重命名、移动、删除、描述

- 前端主区域：
  - `frontend/src/views/User/Main/components/Sidebar/NoteTree/`
  - `frontend/src/views/User/Main/components/Sidebar/RightMenu/`
  - `frontend/src/views/User/Main/components/Sidebar/components/Rename/`
  - `frontend/src/views/User/Main/components/Sidebar/components/Description/`
  - `frontend/src/views/User/Main/components/Sidebar/components/Details/`
- 后端主模块：`backend/src/main/java/org/example/backend/user/note/noteTree/`
- 关键控制器：
  - `GNoteTreeController.java`
  - `PostNoteTreeController.java`
  - `PutNoteTreeController.java`

### 6.4 笔记内容读取、编辑、保存、导出

- 前端页面：`frontend/src/views/User/Main/components/Edit/`
- 前端内容接口：`frontend/src/views/User/Main/components/Edit/service/`
- 后端主模块：`backend/src/main/java/org/example/backend/user/note/note/`
- 关键控制器：
  - `GUNoteController.java`
  - `PUNoteController.java`

### 6.5 笔记封面、图片上传

- 前端封面：`frontend/src/views/User/Main/components/Edit/Main/SetCover/`
- 前端图片保存：`frontend/src/views/User/Main/components/Edit/Main/Service/`
- 后端封面：`backend/src/main/java/org/example/backend/user/note/noteCover/`
- 后端图片：`backend/src/main/java/org/example/backend/user/note/noteImage/`

### 6.6 AI 相关

- 前端 AI 面板：`frontend/src/views/User/Main/components/Edit/PageRight/components/NoteAi/`
- 后端 AI 模块：`backend/src/main/java/org/example/backend/user/ai/`
- 关键接口：
  - `/user/ai/explain`
  - `/user/ai/chat`
  - `/user/ai/stop`

### 6.7 评论相关

- 前端评论面板：`frontend/src/views/User/Main/components/Edit/PageRight/components/NoteComment/`
- 后端评论模块：`backend/src/main/java/org/example/backend/user/note/comment/`
- 管理端评论：`backend/src/main/java/org/example/backend/admin/commentMm/`

### 6.8 待办相关

- 前端页面：`frontend/src/views/User/Main/components/TodoList/`
- 前端请求：
  - `TodoMain/Service/`
  - `TodoListTree/ClassTree/Service/`
- 后端模块：`backend/src/main/java/org/example/backend/user/todo/`
- 关键控制器：
  - `GetUTController.java`
  - `PostUTController.java`
  - `PutUTController.java`
  - `PatchUTController.java`
  - `DeleteUTController.java`

### 6.9 收藏相关

- 前端页面：`frontend/src/views/User/Main/components/MyStar/`
- 后端用户收藏：`backend/src/main/java/org/example/backend/user/note/favoriteNote/`
- 后端管理收藏：`backend/src/main/java/org/example/backend/admin/favoriteMm/`

### 6.10 用户端/管理端仪表盘

- 用户端页面：`frontend/src/views/User/Main/components/Home/`
- 用户端后端：`backend/src/main/java/org/example/backend/user/dashboard/`
- 管理端页面：`frontend/src/views/Admin/Main/view/DashboardMm/`
- 管理端后端：`backend/src/main/java/org/example/backend/admin/dashboard/`

## 7. 前后端联动定位规则

如果需要从前端定位到后端，按下面顺序查：

1. 先看页面对应目录下的 `service/` 或 `Service/`
2. 读取 axios/fetch 的 URL
3. 用完整 URL 去后端搜索 `@RequestMapping`、`@GetMapping`、`@PostMapping` 等
4. 再顺着 controller 找 service、impl、repository

示例：

- 前端 `pwLogin.ts` 请求 `user/auth/PLogin`
  - 对应后端：`user/auth/controller/UserAuthController.java`
- 前端 `SaveNote.ts` 请求 `user/note/SaveNote`
  - 对应后端：`user/note/note/controller/PUNoteController.java`
- 前端 `AddTodo.ts` 请求 `user/todo/add`
  - 对应后端：`user/todo/controller/PostUTController.java`
- 前端管理端 `searchUsers.ts` 请求 `/admin/userMm/search`
  - 对应后端：`admin/userMm/Controller/AdminUserMmController.java`

## 8. 修改时的 Codex 规则

- 修改前端页面行为时，优先在对应页面目录内改，不要先跨模块抽公共层。
- 修改接口时，优先保持当前模块边界：
  - 笔记树改 `noteTree`
  - 笔记正文改 `note`
  - 图片改 `noteImage`
  - 封面改 `noteCover`
  - 评论改 `comment`
  - 收藏改 `favoriteNote`
  - 待办改 `todo`
- 管理端功能优先保持在对应 `*Mm` 模块内，不要把管理逻辑混入用户端模块。
- 新增前端请求文件时，优先放在当前功能目录的 `service/` 或 `Service/` 下，遵循项目现有结构。
- 新增后端接口时，优先放进已有 controller，不要轻易新建分散 controller。
- 搜索问题时优先用精确关键词：
  - 页面名
  - 组件名
  - service 文件名
  - 完整接口路径
  - controller 类名

## 9. 常用搜索建议

优先使用以下搜索方式：

- 查页面入口：搜索路由名或 `component: () => import(...)`
- 查接口来源：搜索 `axios.post(`、`axios.get(`、`fetch(`
- 查后端接口：搜索 `@RequestMapping`、`@GetMapping`、`@PostMapping`
- 查模块边界：搜索目录名，如 `noteTree`、`todoMm`、`favoriteMm`

推荐关键词示例：

- `user/note/SaveNote`
- `user/noteTree`
- `admin/userMm`
- `AccountSettingsController`
- `TodoList.vue`
- `NoteAi.vue`

## 10. 配置与风险提醒

- 前端接口基地址当前写死在：`frontend/src/axios/index.ts`
- 后端数据库、中间件、邮件、AI 等配置集中在：`backend/src/main/resources/application.yml`
- `application.yml` 内含本地开发配置与敏感信息，除非任务明确要求，否则不要在提交中随意改动或外泄。
- 所有涉及数据库表结构变更的修改，必须新增独立的 SQL 更新文件，不能只直接修改已有初始化脚本。
- SQL 更新文件优先放在 `backend/src/main/resources/sql/` 下，按“本次变更目的”命名，确保后续可以独立追溯和执行。

## 11. 本项目的第一定位原则

如果用户没有明确指出文件位置，默认按以下顺序定位：

1. 先找路由入口
2. 再找对应页面组件
3. 再找同目录 `service/` 或 `Service/`
4. 再找后端对应模块 controller
5. 最后下钻 service / impl / repository

按这套规则，通常可以在 1 到 3 次搜索内定位到目标文件。
