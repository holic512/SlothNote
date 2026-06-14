<!--
@file KnowledgeGraph
@project SlothNote
@module 用户端 / 笔记知识星图
@description 展示可拖拽、可缩放的笔记引用知识图谱。
@logic 1. 根据当前笔记加载图谱数据；2. 用 ECharts graph force 布局渲染节点与边；3. 点击节点后通过 noteNavigation 跳转笔记。
@dependencies ECharts: graph/force, Service: getKnowledgeGraph/noteNavigation, Store: currentNoteInfo
@index_tags 知识星图, ECharts, 拖拽图谱, 笔记引用, 节点跳转
@author holic512
-->
<script setup lang="ts">
import {nextTick, onBeforeUnmount, onMounted, ref, watch} from "vue";
import * as echarts from "echarts";
import {Refresh, Share} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
import {useCurrentNoteInfoStore} from "@/views/User/Main/components/Edit/Pinia/currentNoteInfo";
import {navigateToNote} from "@/views/User/Main/components/Edit/service/noteNavigation";
import {
  getKnowledgeGraph,
  type KnowledgeGraphData,
  type KnowledgeGraphNode
} from "@/views/User/Main/components/Edit/PageRight/components/KnowledgeGraph/service/getKnowledgeGraph";

const router = useRouter();
const currentNoteInfo = useCurrentNoteInfoStore();
const chartRef = ref<HTMLDivElement>();
const loading = ref(false);
const graphData = ref<KnowledgeGraphData>({
  currentNoteId: null,
  nodes: [],
  edges: [],
});

let chart: echarts.ECharts | null = null;

const loadGraph = async () => {
  loading.value = true;
  try {
    graphData.value = await getKnowledgeGraph(currentNoteInfo.noteId);
    await nextTick();
    renderGraph();
  } catch (error) {
    console.error(error);
    ElMessage.error("知识星图加载失败");
  } finally {
    loading.value = false;
  }
};

const renderGraph = () => {
  if (!chartRef.value) {
    return;
  }

  if (!chart) {
    chart = echarts.init(chartRef.value);
    chart.on("click", {dataType: "node"} as any, async (params: any) => {
      const node = params.data as KnowledgeGraphNode | undefined;
      if (!node?.noteId) {
        return;
      }
      await navigateToNote(router, {
        noteId: node.noteId,
        noteName: node.title,
      });
    });
  }

  const nodes = graphData.value.nodes.map(node => ({
    ...node,
    id: String(node.noteId),
    name: node.title || "新建文档",
    value: node.degree,
    symbolSize: node.current ? 56 : Math.max(30, Math.min(48, 30 + node.degree * 5)),
    draggable: true,
    label: {
      show: true,
      formatter: node.avatar ? `${node.avatar} ${node.title || "新建文档"}` : node.title || "新建文档",
      color: node.current ? "#203324" : "#445047",
      fontSize: node.current ? 12 : 11,
      fontWeight: node.current ? 700 : 500,
    },
    itemStyle: {
      color: node.current ? "#86B36A" : node.degree > 0 ? "#CBDDBD" : "#E7E2D3",
      borderColor: node.current ? "#2F5E35" : "#8EA184",
      borderWidth: node.current ? 3 : 1,
    },
  }));

  const links = graphData.value.edges.map(edge => ({
    source: String(edge.sourceNoteId),
    target: String(edge.targetNoteId),
    value: edge.labelSnapshot || "引用",
    lineStyle: {
      width: 1.5,
      color: "#9AAA8F",
      curveness: 0.18,
    },
  }));

  chart.setOption({
    backgroundColor: "transparent",
    tooltip: {
      trigger: "item",
      formatter(params: any) {
        if (params.dataType === "edge") {
          return "引用关系";
        }
        const node = params.data as KnowledgeGraphNode;
        return `<strong>${node.title || "新建文档"}</strong><br/>引用连接：${node.degree || 0}`;
      },
    },
    series: [
      {
        type: "graph",
        layout: "force",
        data: nodes,
        links,
        roam: true,
        draggable: true,
        focusNodeAdjacency: true,
        edgeSymbol: ["none", "arrow"],
        edgeSymbolSize: 8,
        force: {
          repulsion: 260,
          edgeLength: [70, 150],
          gravity: 0.08,
          friction: 0.4,
        },
        emphasis: {
          focus: "adjacency",
          lineStyle: {
            width: 3,
          },
        },
      },
    ],
  }, true);
};

const resizeChart = () => {
  chart?.resize();
};

watch(() => currentNoteInfo.noteId, () => {
  void loadGraph();
});

onMounted(() => {
  void loadGraph();
  window.addEventListener("resize", resizeChart);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resizeChart);
  chart?.dispose();
  chart = null;
});
</script>

<template>
  <section class="knowledge-graph">
    <header class="graph-header">
      <div>
        <div class="graph-title">
          <el-icon size="17">
            <Share/>
          </el-icon>
          知识星图
        </div>
        <div class="graph-subtitle">
          {{ graphData.nodes.length }} 篇笔记，{{ graphData.edges.length }} 条引用
        </div>
      </div>

      <el-tooltip effect="dark" content="刷新图谱" :show-after="500" placement="bottom">
        <el-button text class="refresh-button" :loading="loading" @click="loadGraph">
          <el-icon size="16">
            <Refresh/>
          </el-icon>
        </el-button>
      </el-tooltip>
    </header>

    <div class="graph-stage" v-loading="loading">
      <div v-if="graphData.nodes.length" ref="chartRef" class="graph-canvas"/>
      <div v-else class="graph-empty">
        <div class="empty-title">暂无星图</div>
        <div class="empty-text">在编辑器中插入“引用笔记”，保存后会生成连接。</div>
      </div>
    </div>

    <footer class="graph-footer">
      <span>拖拽节点整理结构</span>
      <span>滚轮缩放，点击节点跳转</span>
    </footer>
  </section>
</template>

<style scoped>
.knowledge-graph {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #FBFBF8;
  color: #2C332D;
}

.graph-header {
  height: 58px;
  padding: 10px 14px;
  border-bottom: 1px solid #ECEEE8;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.graph-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
}

.graph-subtitle {
  margin-top: 3px;
  color: #7D877B;
  font-size: 12px;
}

.refresh-button {
  width: 30px;
  height: 30px;
}

.graph-stage {
  flex: 1;
  min-height: 0;
  position: relative;
  background:
      linear-gradient(rgba(116, 132, 106, 0.08) 1px, transparent 1px),
      linear-gradient(90deg, rgba(116, 132, 106, 0.08) 1px, transparent 1px),
      #FBFBF8;
  background-size: 22px 22px;
}

.graph-canvas {
  width: 100%;
  height: 100%;
}

.graph-empty {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #788174;
  text-align: center;
  padding: 24px;
}

.empty-title {
  color: #2C332D;
  font-weight: 700;
}

.empty-text {
  max-width: 260px;
  font-size: 12px;
  line-height: 1.6;
}

.graph-footer {
  height: 34px;
  padding: 0 14px;
  border-top: 1px solid #ECEEE8;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #7D877B;
  font-size: 11px;
}
</style>
