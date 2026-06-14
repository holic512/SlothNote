/**
 * @file treeInterface
 * @project SlothNote
 * @module 用户端 / 笔记树
 * @description 定义侧边栏笔记树节点结构。
 * @logic 记录笔记/文件夹 ID、标题、类型、子节点、头像、封面和前端唯一 key。
 * @dependencies Backend DTO: NoteTreeDto
 * @index_tags 笔记树类型, Tree, 侧边栏, 笔记元数据
 * @author holic512
 */
export interface Tree {
    id: number;
    label: string;
    type: string;
    children?: Tree[] | null;
    avatar?: string;
    cover?: string | null;
    uniqueId?: string;
}
