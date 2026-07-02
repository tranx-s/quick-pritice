<template>
  <div>
    <!-- 统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:16px;">
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-num">{{ stats.total ?? '-' }}</div>
            <div class="stat-label">题目总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-num" style="color:#67c23a;">{{ stats.online ?? '-' }}</div>
            <div class="stat-label">已上架</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <div class="stat-item">
            <div class="stat-num" style="color:#f56c6c;">{{ stats.offline ?? '-' }}</div>
            <div class="stat-label">已下架</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <!-- 筛选栏 -->
      <el-form :inline="true" :model="filter" style="margin-bottom:12px;">
        <el-form-item label="模块">
          <el-select v-model="filter.moduleType" clearable placeholder="全部模块" style="width:130px;">
            <el-option v-for="t in moduleTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="filter.source" clearable placeholder="全部来源" style="width:120px;">
            <el-option v-for="s in sources" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filter.isOnline" clearable placeholder="全部状态" style="width:110px;">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="filter.keyword" placeholder="搜索题目内容" clearable style="width:200px;"
            @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
        <el-form-item style="float:right;">
          <el-button type="danger" :disabled="!selection.length" @click="batchDelete">
            批量删除({{ selection.length }})
          </el-button>
          <el-button type="primary" @click="openEdit(null)">
            <el-icon><Plus /></el-icon> 新增题目
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="tableData" border stripe v-loading="loading"
        @selection-change="selection = $event" style="width:100%;">
        <el-table-column type="selection" width="44" />
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="moduleType" label="模块" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="moduleColor(row.moduleType)">{{ row.moduleType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="题目内容" min-width="280" show-overflow-tooltip />
        <el-table-column prop="correctAnswer" label="答案" width="65" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.correctAnswer }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.source || 'manual' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isOnline" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isOnline ? 'success' : 'danger'" size="small">
              {{ row.isOnline ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="175" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.isOnline ? 'warning' : 'success'"
              @click="toggleOnline(row)">
              {{ row.isOnline ? '下架' : '上架' }}
            </el-button>
            <el-button size="small" type="danger" @click="deleteOne(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="text-align:right;margin-top:16px;">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next"
          @change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑题目' : '新增题目'"
      width="700px" destroy-on-close>
      <el-form :model="editForm" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="模块类型" prop="moduleType">
          <el-select v-model="editForm.moduleType" style="width:100%;">
            <el-option v-for="t in moduleTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="editForm.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="选项A" prop="optionA">
              <el-input v-model="editForm.optionA" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选项B" prop="optionB">
              <el-input v-model="editForm.optionB" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="选项C" prop="optionC">
              <el-input v-model="editForm.optionC" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选项D" prop="optionD">
              <el-input v-model="editForm.optionD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="正确答案" prop="correctAnswer">
          <el-radio-group v-model="editForm.correctAnswer">
            <el-radio-button label="A">A</el-radio-button>
            <el-radio-button label="B">B</el-radio-button>
            <el-radio-button label="C">C</el-radio-button>
            <el-radio-button label="D">D</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="答案解析">
          <el-input v-model="editForm.analysis" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="是否上架">
          <el-switch v-model="editForm.isOnline" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { questionApi } from '@/api/question'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const selection = ref([])
const moduleTypes = ref([])
const sources = ref([])
const stats = ref({})
const editVisible = ref(false)
const formRef = ref()

const filter = reactive({ moduleType: '', source: '', isOnline: null, keyword: '' })
const pagination = reactive({ page: 1, size: 20, total: 0 })
const editForm = reactive({
  id: null, moduleType: '', content: '', optionA: '', optionB: '',
  optionC: '', optionD: '', correctAnswer: 'A', analysis: '', isOnline: 1
})

const rules = {
  moduleType: [{ required: true, message: '请选择模块' }],
  content: [{ required: true, message: '请输入题目内容' }],
  optionA: [{ required: true, message: '请输入选项A' }],
  optionB: [{ required: true, message: '请输入选项B' }],
  optionC: [{ required: true, message: '请输入选项C' }],
  optionD: [{ required: true, message: '请输入选项D' }],
  correctAnswer: [{ required: true, message: '请选择正确答案' }]
}

const moduleColorMap = { '言语理解': '', '数量关系': 'warning', '判断推理': 'success', '政治理论': 'danger' }
const moduleColor = (t) => moduleColorMap[t] || 'info'

async function loadData() {
  loading.value = true
  try {
    const res = await questionApi.page({
      page: pagination.page,
      size: pagination.size,
      moduleType: filter.moduleType || undefined,
      source: filter.source || undefined,
      isOnline: filter.isOnline ?? undefined,
      keyword: filter.keyword || undefined
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  const res = await questionApi.stats()
  stats.value = res.data || {}
}

function resetFilter() {
  Object.assign(filter, { moduleType: '', source: '', isOnline: null, keyword: '' })
  pagination.page = 1
  loadData()
}

function openEdit(row) {
  if (row) {
    Object.assign(editForm, { ...row })
  } else {
    Object.assign(editForm, {
      id: null, moduleType: moduleTypes.value[0] || '', content: '',
      optionA: '', optionB: '', optionC: '', optionD: '',
      correctAnswer: 'A', analysis: '', isOnline: 1
    })
  }
  editVisible.value = true
}

async function saveQuestion() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (editForm.id) {
      await questionApi.update(editForm.id, editForm)
    } else {
      await questionApi.add(editForm)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    loadData()
    loadStats()
  } finally {
    saving.value = false
  }
}

async function toggleOnline(row) {
  await questionApi.toggle(row.id)
  ElMessage.success(row.isOnline ? '已下架' : '已上架')
  loadData()
  loadStats()
}

async function deleteOne(row) {
  await ElMessageBox.confirm(`确认删除该题目？`, '提示', { type: 'warning' })
  await questionApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
  loadStats()
}

async function batchDelete() {
  await ElMessageBox.confirm(`确认删除选中的 ${selection.value.length} 道题目？`, '提示', { type: 'warning' })
  await questionApi.batchDelete(selection.value.map(r => r.id))
  ElMessage.success('批量删除成功')
  loadData()
  loadStats()
}

onMounted(async () => {
  const [mt, sr] = await Promise.all([questionApi.moduleTypes(), questionApi.sources()])
  moduleTypes.value = mt.data || []
  sources.value = sr.data || []
  loadData()
  loadStats()
})
</script>

<style scoped>
.stat-item { text-align: center; padding: 8px 0; }
.stat-num { font-size: 28px; font-weight: 700; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>
