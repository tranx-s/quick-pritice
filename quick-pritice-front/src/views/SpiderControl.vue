<template>
  <div>
    <el-row :gutter="16">
      <!-- 左侧：触发爬取 -->
      <el-col :span="10">
        <el-card shadow="never" header="触发爬取任务">
          <el-form :model="form" label-width="90px">
            <el-form-item label="目标网站">
              <el-checkbox-group v-model="form.sources">
                <el-checkbox label="fenbi">粉笔</el-checkbox>
                <el-checkbox label="offcn">中公</el-checkbox>
                <el-checkbox label="huatu">华图</el-checkbox>
                <el-checkbox label="yuantiku">猿题库</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="模块类型">
              <el-checkbox-group v-model="form.moduleTypes">
                <el-checkbox v-for="t in allModuleTypes" :key="t" :label="t">{{ t }}</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="爬取页数">
              <el-input-number v-model="form.pageCount" :min="1" :max="100" />
              <span style="margin-left:8px; color:#909399; font-size:13px;">页（约每页20题）</span>
            </el-form-item>
            <el-form-item label="粉笔Cookie" v-if="form.sources.includes('fenbi')">
              <el-input v-model="form.fenbiCookie" type="textarea" :rows="3"
                placeholder="登录粉笔后从浏览器开发者工具复制 Cookie 字符串" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="starting" :disabled="!canStart" @click="startScrape">
                <el-icon><VideoPlay /></el-icon> 开始爬取
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 历史任务列表 -->
        <el-card shadow="never" header="历史任务" style="margin-top:16px;">
          <el-table :data="taskList" size="small" v-loading="loadingTasks">
            <el-table-column prop="task_id" label="任务ID" width="80" />
            <el-table-column prop="source" label="来源" width="80" />
            <el-table-column prop="module_type" label="模块" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="count" label="入库数" width="65" align="center" />
            <el-table-column label="操作" width="60">
              <template #default="{ row }">
                <el-button size="small" link @click="viewTask(row.task_id)">日志</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div style="text-align:center; margin-top:10px;">
            <el-button size="small" @click="loadTasks">刷新</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：实时进度 + 日志 -->
      <el-col :span="14">
        <el-card shadow="never" :header="`任务进度 ${activeTaskId ? '#' + activeTaskId : ''}`">
          <div v-if="!activeTaskId" style="text-align:center; color:#c0c4cc; padding:30px 0;">
            触发爬取后将在此显示进度
          </div>
          <div v-else>
            <el-descriptions :column="3" size="small" border style="margin-bottom:12px;">
              <el-descriptions-item label="状态">
                <el-tag :type="statusType(currentTask.status)" size="small">
                  {{ currentTask.status }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="已爬取">{{ currentTask.scraped ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="已入库">{{ currentTask.saved ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="重复跳过">{{ currentTask.skipped ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="失败">{{ currentTask.failed ?? 0 }}</el-descriptions-item>
              <el-descriptions-item label="进度">
                {{ currentTask.current_page ?? 0 }} / {{ currentTask.total_pages ?? 0 }} 页
              </el-descriptions-item>
            </el-descriptions>
            <el-progress
              :percentage="progress"
              :status="currentTask.status === 'done' ? 'success' : currentTask.status === 'error' ? 'exception' : undefined"
            />
          </div>
        </el-card>

        <el-card shadow="never" header="实时日志" style="margin-top:16px;">
          <div ref="logBoxRef" class="log-box">
            <div v-for="(line, i) in logs" :key="i" :class="['log-line', logClass(line)]">{{ line }}</div>
            <div v-if="!logs.length" style="color:#606266; font-size:13px;">暂无日志</div>
          </div>
          <div style="margin-top:8px; text-align:right;">
            <el-button size="small" @click="logs = []">清空</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { spiderApi } from '@/api/spider'

const allModuleTypes = ['言语理解', '数量关系', '判断推理', '政治理论']

const form = reactive({
  sources: ['fenbi'],
  moduleTypes: ['言语理解', '数量关系', '判断推理', '政治理论'],
  pageCount: 5,
  fenbiCookie: ''
})

const starting = ref(false)
const loadingTasks = ref(false)
const taskList = ref([])
const activeTaskId = ref(null)
const currentTask = ref({})
const logs = ref([])
const logBoxRef = ref()
let pollTimer = null

const canStart = computed(() => form.sources.length > 0 && form.moduleTypes.length > 0)
const progress = computed(() => {
  const t = currentTask.value
  if (!t.total_pages) return 0
  return Math.round((t.current_page / t.total_pages) * 100)
})

const statusType = (s) => ({
  running: 'primary', done: 'success', error: 'danger',
  pending: 'info', skipped: 'warning'
}[s] || 'info')

const logClass = (line) => {
  if (line.includes('[ERROR]') || line.includes('失败')) return 'log-error'
  if (line.includes('[SUCCESS]') || line.includes('入库')) return 'log-success'
  if (line.includes('[WARN]') || line.includes('跳过')) return 'log-warn'
  return ''
}

function scrollLogsToBottom() {
  nextTick(() => {
    if (logBoxRef.value) logBoxRef.value.scrollTop = logBoxRef.value.scrollHeight
  })
}

async function startScrape() {
  starting.value = true
  try {
    // 对每个来源+模块类型组合启动任务
    const tasks = []
    for (const source of form.sources) {
      for (const moduleType of form.moduleTypes) {
        const body = {
          source,
          module_type: moduleType,
          page_count: form.pageCount,
          cookie: source === 'fenbi' ? form.fenbiCookie : undefined
        }
        const res = await spiderApi.start(body)
        if (res.task_id) {
          tasks.push(res.task_id)
          logs.value.push(`[INFO] 任务启动: ${source} - ${moduleType} (ID: ${res.task_id})`)
        }
      }
    }
    if (tasks.length > 0) {
      activeTaskId.value = tasks[0]
      startPolling(tasks[0])
      loadTasks()
      ElMessage.success(`已启动 ${tasks.length} 个爬取任务`)
    }
  } catch (e) {
    ElMessage.error('启动失败，请确认爬虫服务已运行（端口 8002）')
    logs.value.push('[ERROR] 爬虫服务连接失败，请先启动 quick-pritice-spider')
  } finally {
    starting.value = false
    scrollLogsToBottom()
  }
}

function startPolling(taskId) {
  clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    try {
      const [statusRes, logRes] = await Promise.all([
        spiderApi.status(taskId),
        spiderApi.logs(taskId)
      ])
      currentTask.value = statusRes
      if (logRes.logs) {
        logs.value = logRes.logs
        scrollLogsToBottom()
      }
      if (statusRes.status === 'done' || statusRes.status === 'error') {
        clearInterval(pollTimer)
        loadTasks()
      }
    } catch {}
  }, 2000)
}

async function viewTask(taskId) {
  activeTaskId.value = taskId
  startPolling(taskId)
}

async function loadTasks() {
  loadingTasks.value = true
  try {
    const res = await spiderApi.tasks()
    taskList.value = res.tasks || []
  } catch {
    taskList.value = []
  } finally {
    loadingTasks.value = false
  }
}

onBeforeUnmount(() => clearInterval(pollTimer))
</script>

<style scoped>
.log-box {
  background: #1e1e2e;
  border-radius: 6px;
  padding: 12px 14px;
  height: 320px;
  overflow-y: auto;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12.5px;
  line-height: 1.7;
}
.log-line { color: #cdd6f4; }
.log-error { color: #f38ba8; }
.log-success { color: #a6e3a1; }
.log-warn { color: #f9e2af; }
</style>
