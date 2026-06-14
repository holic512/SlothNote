<!--
@file NoteReferencePicker
@project SlothNote
@module 用户端 / 编辑器工具栏
@description 提供笔记引用搜索与插入入口，将其他笔记插入为 Tiptap noteReference 节点。
@logic 1. 输入关键词搜索用户笔记；2. 过滤当前笔记；3. 选择结果后插入 noteReference inline atom。
@dependencies Service: searchNotes, Store: currentNoteInfo, Tiptap: Editor
@index_tags 笔记引用, 工具栏, Tiptap, 搜索笔记, noteReference
@author holic512
-->
<script setup lang="ts">
import {computed, ref, watch} from "vue";
import type {ModelRef} from "vue";
import type {Editor} from "@tiptap/vue-3";
import {ElMessage} from "element-plus";
import {Link, Search} from "@element-plus/icons-vue";
import {searchNotes} from "@/views/User/Main/components/SidebarM/service/searchNotes";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";

interface SearchNoteResult {
  noteId: number;
  title?: string;
  summary?: string;
  snippet?: string;
}

const editor: ModelRef<Editor | undefined> = defineModel();
const currentNoteInfo = useCurrentNoteInfoStore();
const visible = ref(false);
const keyword = ref("");
const loading = ref(false);
const results = ref<SearchNoteResult[]>([]);

const visibleResults = computed(() => {
  return results.value.filter(item => item.noteId !== currentNoteInfo.noteId);
});

let searchTimer: ReturnType<typeof setTimeout> | undefined;

watch(keyword, () => {
  if (searchTimer) {
    clearTimeout(searchTimer);
  }
  searchTimer = setTimeout(() => {
    void reloadResults();
  }, 220);
});

const reloadResults = async () => {
  const q = keyword.value.trim();
  if (!q) {
    results.value = [];
    return;
  }

  loading.value = true;
  try {
    results.value = await searchNotes(q);
  } finally {
    loading.value = false;
  }
};

const insertReference = (note: SearchNoteResult) => {
  if (!editor.value) {
    ElMessage.warning("编辑器尚未初始化");
    return;
  }

  editor.value.chain().focus().insertContent({
    type: "noteReference",
    attrs: {
      noteId: note.noteId,
      title: note.title || "新建文档",
    },
  }).insertContent(" ").run();

  visible.value = false;
  keyword.value = "";
  results.value = [];
};
</script>

<template>
  <el-popover
      v-model:visible="visible"
      placement="bottom-start"
      :width="320"
      trigger="click"
      popper-class="note-reference-popover"
  >
    <template #reference>
      <el-tooltip effect="dark" content=" 引用笔记 " :show-after="500" placement="bottom">
        <el-button text class="tool-button reference-trigger">
          <el-icon size="17" color="#000000">
            <Link/>
          </el-icon>
        </el-button>
      </el-tooltip>
    </template>

    <div class="reference-panel">
      <el-input
          v-model="keyword"
          size="small"
          placeholder="搜索要引用的笔记"
          :prefix-icon="Search"
          clearable
          @keyup.enter="reloadResults"
      />

      <div v-loading="loading" class="reference-results">
        <button
            v-for="note in visibleResults"
            :key="note.noteId"
            class="reference-row"
            type="button"
            @click="insertReference(note)"
        >
          <span class="reference-title">{{ note.title || '新建文档' }}</span>
          <span class="reference-summary">{{ note.summary || note.snippet || '无摘要' }}</span>
        </button>

        <el-empty
            v-if="keyword && !loading && visibleResults.length === 0"
            description="没有可引用的笔记"
            :image-size="56"
        />
        <div v-if="!keyword" class="reference-hint">输入标题、摘要或正文关键词</div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.reference-trigger {
  width: 30px;
  height: 30px;
}

.reference-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.reference-results {
  min-height: 140px;
  max-height: 260px;
  overflow: auto;
}

.reference-row {
  width: 100%;
  border: 0;
  border-bottom: 1px solid #EEF0EA;
  background: transparent;
  padding: 9px 4px;
  display: flex;
  flex-direction: column;
  gap: 3px;
  text-align: left;
  cursor: pointer;
}

.reference-row:hover {
  background: #F4F7F1;
}

.reference-title {
  color: #253427;
  font-size: 13px;
  font-weight: 650;
}

.reference-summary {
  color: #7A8578;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reference-hint {
  padding: 34px 0;
  color: #8B9389;
  font-size: 12px;
  text-align: center;
}
</style>
