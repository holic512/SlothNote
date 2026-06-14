/**
 * @file ElementCompatIconMap
 * @project SlothNote
 * @module 前端组件 / 旧组件迁移兼容层
 * @description 将旧图标语义映射为 Element Plus 图标组件。
 * @logic 1. 接收迁移后的图标名称；2. 返回对应 Element Plus 图标组件；3. 未匹配时返回 undefined 让组件隐藏图标。
 * @dependencies @element-plus/icons-vue
 * @index_tags 图标映射, ElementPlus迁移, ElementPlus图标
 * @author holic512
 */
import {
  Aim,
  ArrowLeft,
  ArrowRight,
  Back,
  Collection,
  Check,
  CircleCheckFilled,
  CloseBold,
  Comment,
  DArrowLeft,
  DArrowRight,
  Delete,
  EditPen,
  Filter,
  Folder,
  Hide,
  HomeFilled,
  MessageBox,
  MoreFilled,
  Notebook,
  Plus,
  Refresh,
  RefreshRight,
  Search,
  Setting,
  StarFilled,
  SwitchButton,
  Tickets,
  User,
  View,
  Warning,
} from '@element-plus/icons-vue'
import type {Component} from 'vue'

const iconMap: Record<string, Component> = {
  Aim,
  AngleDoubleLeft: DArrowLeft,
  AngleDoubleRight: DArrowRight,
  AngleLeft: ArrowLeft,
  AngleRight: ArrowRight,
  Back,
  Ban: CloseBold,
  Book: Notebook,
  Bookmark: Collection,
  Calendar: Tickets,
  Check,
  CheckSquare: CircleCheckFilled,
  CircleFill: CircleCheckFilled,
  Cog: Setting,
  Comments: Comment,
  Database: Tickets,
  Delete,
  Edit: EditPen,
  Ellipsis: MoreFilled,
  Eye: View,
  Filter,
  FilterSlash: Filter,
  Folder,
  Home: HomeFilled,
  Inbox: MessageBox,
  Notebook,
  PenToSquare: EditPen,
  Plus,
  Refresh,
  Restore: RefreshRight,
  Search,
  Setting,
  Spinner: Refresh,
  Star: StarFilled,
  Trash: Delete,
  User,
  Users: User,
  Warning,
}

export const resolveCompatIcon = (icon?: string | Component): Component | string | undefined => {
  if (!icon) return undefined
  if (typeof icon !== 'string') return icon
  return iconMap[icon] ?? undefined
}
