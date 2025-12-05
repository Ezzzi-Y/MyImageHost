<template>
  <div class="admin-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>管理员后台</h2>
          <div class="user-info">
            <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            <el-tag type="danger" size="small">管理员</el-tag>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>
      
      <el-main>
        <el-tabs v-model="activeTab">
          <!-- 用户管理 -->
          <el-tab-pane label="用户管理" name="users">
            <el-card shadow="hover">
              <!-- 搜索区域 -->
              <el-row :gutter="20" style="margin-bottom: 20px">
                <el-col :span="6">
                  <el-input v-model="searchForm.email" placeholder="邮箱" clearable />
                </el-col>
                <el-col :span="6">
                  <el-input v-model="searchForm.nickname" placeholder="昵称" clearable />
                </el-col>
                <el-col :span="6">
                  <el-select v-model="searchForm.enabled" placeholder="账号状态" clearable>
                    <el-option label="启用" :value="true" />
                    <el-option label="禁用" :value="false" />
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="handleSearch">搜索</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-col>
              </el-row>

              <!-- 用户列表 -->
              <el-table :data="users" style="width: 100%" v-loading="loading">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
                <el-table-column prop="nickname" label="昵称" width="120" />
                <el-table-column prop="role" label="角色" width="100">
                  <template #default="scope">
                    <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'" size="small">
                      {{ scope.row.role === 'ADMIN' ? '管理员' : '用户' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="enabled" label="状态" width="80">
                  <template #default="scope">
                    <el-tag :type="scope.row.enabled ? 'success' : 'danger'" size="small">
                      {{ scope.row.enabled ? '启用' : '禁用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="空间配额" width="150">
                  <template #default="scope">
                    <div>{{ formatSize(scope.row.usedSpace) }} / {{ formatSize(scope.row.quotaSpace) }}</div>
                    <el-progress 
                      :percentage="calculatePercentage(scope.row.usedSpace, scope.row.quotaSpace)" 
                      :format="() => ''"
                      :stroke-width="6"
                    />
                  </template>
                </el-table-column>
                <el-table-column label="数量配额" width="120">
                  <template #default="scope">
                    <div>{{ scope.row.usedCount }} / {{ scope.row.quotaCount }}</div>
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="封禁信息" width="150">
                  <template #default="scope">
                    <div v-if="!scope.row.enabled && scope.row.disabledUntil">
                      <el-tooltip :content="`原因: ${scope.row.disableReason || '无'}`" placement="top">
                        <el-tag type="warning" size="small">
                          {{ formatDate(scope.row.disabledUntil) }}到期
                        </el-tag>
                      </el-tooltip>
                    </div>
                    <span v-else-if="!scope.row.enabled" style="color: #909399; font-size: 12px;">
                      永久禁用
                    </span>
                    <span v-else style="color: #67c23a; font-size: 12px;">
                      正常
                    </span>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="320" fixed="right">
                  <template #default="scope">
                    <el-button 
                      v-if="!scope.row.enabled" 
                      size="small" 
                      type="info"
                      @click="viewBanDetail(scope.row)"
                    >
                      查看详情
                    </el-button>
                    <el-button size="small" @click="openQuotaDialog(scope.row)">
                      修改配额
                    </el-button>
                    <el-button 
                      v-if="scope.row.enabled" 
                      size="small" 
                      type="warning" 
                      @click="openDisableDialog(scope.row)"
                    >
                      禁用
                    </el-button>
                    <el-button 
                      v-else 
                      size="small" 
                      type="success" 
                      @click="handleEnableUser(scope.row.id)"
                    >
                      启用
                    </el-button>
                    <el-button 
                      size="small" 
                      type="danger" 
                      @click="handleDeleteUser(scope.row.id)"
                      :disabled="scope.row.role === 'ADMIN'"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <!-- 修改配额对话框 -->
    <el-dialog v-model="quotaDialogVisible" title="修改用户配额" width="500px">
      <el-form :model="quotaForm" label-width="120px">
        <el-form-item label="用户邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>
        <el-form-item label="空间配额 (MB)">
          <el-input-number 
            v-model="quotaForm.spaceMB" 
            :min="0" 
            :max="10240"
            :step="100"
          />
        </el-form-item>
        <el-form-item label="数量配额">
          <el-input-number 
            v-model="quotaForm.count" 
            :min="0" 
            :max="10000"
            :step="10"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="quotaDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveQuota">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 禁用用户对话框 -->
    <el-dialog v-model="disableDialogVisible" title="禁用用户" width="500px">
      <el-form :model="disableForm" label-width="120px">
        <el-form-item label="用户邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>
        <el-form-item label="禁用时长 (天)">
          <el-input-number 
            v-model="disableForm.durationDays" 
            :min="1" 
            :max="365"
          />
        </el-form-item>
        <el-form-item label="禁用原因">
          <el-input 
            v-model="disableForm.reason" 
            type="textarea" 
            :rows="3"
            placeholder="请输入禁用原因"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="disableDialogVisible = false">取消</el-button>
          <el-button type="warning" @click="handleDisableUser">
            确定禁用
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看封禁详情对话框 -->
    <el-dialog v-model="banDetailVisible" title="封禁详情" width="500px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="用户邮箱">
          {{ banDetail.email }}
        </el-descriptions-item>
        <el-descriptions-item label="用户昵称">
          {{ banDetail.nickname }}
        </el-descriptions-item>
        <el-descriptions-item label="封禁状态">
          <el-tag type="danger" size="small">已封禁</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="封禁原因">
          <div style="white-space: pre-wrap; color: #f56c6c;">
            {{ banDetail.disableReason || '无' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="封禁到期时间">
          <span v-if="banDetail.disabledUntil" style="font-weight: 600;">
            {{ banDetail.disabledUntil }}
          </span>
          <el-tag v-else type="info" size="small">永久封禁</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="剩余天数">
          <span v-if="banDetail.disabledUntil" style="color: #e6a23c; font-weight: 600;">
            {{ calculateRemainingDays(banDetail.disabledUntil) }} 天
          </span>
          <span v-else style="color: #909399;">-</span>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="banDetailVisible = false">关闭</el-button>
          <el-button type="success" @click="handleEnableUserFromDetail">
            立即解封
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  listUsers, 
  disableUser, 
  enableUser, 
  deleteUser, 
  updateSpace,
  searchUser 
} from '@/api/admin'
import type { User } from '@/types'

const userStore = useUserStore()
const activeTab = ref('users')
const users = ref<User[]>([])
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  email: '',
  nickname: '',
  enabled: undefined as boolean | undefined
})

// 配额对话框
const quotaDialogVisible = ref(false)
const currentUser = reactive({
  id: 0,
  email: ''
})
const quotaForm = reactive({
  spaceMB: 100,
  count: 100
})

// 禁用对话框
const disableDialogVisible = ref(false)
const disableForm = reactive({
  durationDays: 7,
  reason: ''
})

// 封禁详情对话框
const banDetailVisible = ref(false)
const banDetail = reactive({
  id: 0,
  email: '',
  nickname: '',
  disableReason: '',
  disabledUntil: ''
})

// 工具函数
const formatSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const calculatePercentage = (used: number, total: number) => {
  if (!total) return 0
  return Math.min(100, Math.round((used / total) * 100))
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const now = new Date()
  const diffTime = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  if (diffDays < 0) return '已过期'
  if (diffDays === 0) return '今天到期'
  if (diffDays === 1) return '明天到期'
  return `${diffDays}天后`
}

const calculateRemainingDays = (dateStr: string) => {
  if (!dateStr) return 0
  const date = new Date(dateStr)
  const now = new Date()
  const diffTime = date.getTime() - now.getTime()
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  return Math.max(0, diffDays)
}

// 查看封禁详情
const viewBanDetail = (user: User) => {
  banDetail.id = user.id!
  banDetail.email = user.email
  banDetail.nickname = user.nickname
  banDetail.disableReason = user.disableReason || ''
  banDetail.disabledUntil = user.disabledUntil || ''
  banDetailVisible.value = true
}

// 从详情页解封
const handleEnableUserFromDetail = async () => {
  try {
    await ElMessageBox.confirm('确定要解封该用户吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await enableUser(banDetail.id)
    ElMessage.success('用户已解封')
    banDetailVisible.value = false
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('解封用户失败:', error)
    }
  }
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    const res = await listUsers()
    users.value = res.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = async () => {
  try {
    loading.value = true
    const params: any = {}
    if (searchForm.email) params.email = searchForm.email
    if (searchForm.nickname) params.nickname = searchForm.nickname
    if (searchForm.enabled !== undefined) params.enabled = searchForm.enabled
    
    const res = await searchUser(params)
    users.value = res.data
  } catch (error) {
    console.error('搜索失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置搜索
const handleReset = () => {
  searchForm.email = ''
  searchForm.nickname = ''
  searchForm.enabled = undefined
  fetchUsers()
}

// 打开配额对话框
const openQuotaDialog = (user: User) => {
  currentUser.id = user.id!
  currentUser.email = user.email
  quotaForm.spaceMB = Math.round(user.quotaSpace / (1024 * 1024))
  quotaForm.count = user.quotaCount
  quotaDialogVisible.value = true
}

// 保存配额
const handleSaveQuota = async () => {
  try {
    const quotaSpace = quotaForm.spaceMB * 1024 * 1024
    await updateSpace({
      id: currentUser.id,
      quotaSpace,
      quotaCount: quotaForm.count
    })
    ElMessage.success('配额修改成功')
    quotaDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('修改配额失败:', error)
  }
}

// 打开禁用对话框
const openDisableDialog = (user: User) => {
  currentUser.id = user.id!
  currentUser.email = user.email
  disableForm.durationDays = 7
  disableForm.reason = ''
  disableDialogVisible.value = true
}

// 禁用用户
const handleDisableUser = async () => {
  if (!disableForm.reason.trim()) {
    ElMessage.warning('请输入禁用原因')
    return
  }

  try {
    await disableUser({
      userId: currentUser.id,
      reason: disableForm.reason,
      durationDays: disableForm.durationDays
    })
    ElMessage.success('用户已禁用')
    disableDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('禁用用户失败:', error)
  }
}

// 启用用户
const handleEnableUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要启用该用户吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await enableUser(id)
    ElMessage.success('用户已启用')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('启用用户失败:', error)
    }
  }
}

// 删除用户
const handleDeleteUser = async (id: number) => {
  try {
    await ElMessageBox.confirm(
      '删除用户将同时删除该用户的所有图片，确定要删除吗?', 
      '警告', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteUser(id)
    ElMessage.success('用户已删除')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}

// 登出
const handleLogout = () => {
  userStore.logout()
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #ffd7d7 100%);
  animation: fadeIn 0.5s ease;
}

.header {
  background: linear-gradient(135deg, #f56c6c 0%, #ff5757 100%);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.header-content h2 {
  margin: 0;
  color: #fff;
  font-size: 24px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nickname {
  font-weight: 600;
  color: #fff;
  font-size: 15px;
}

.el-main {
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

:deep(.el-card) {
  border-radius: 12px;
  border: none;
  animation: slideInUp 0.6s ease;
  transition: all 0.3s ease;
}

:deep(.el-card:hover) {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0, 0, 0, 0.12);
}

:deep(.el-tabs__header) {
  margin-bottom: 24px;
}

:deep(.el-tabs__item) {
  font-size: 16px;
  font-weight: 500;
  padding: 0 24px;
  height: 48px;
  line-height: 48px;
  transition: all 0.3s ease;
}

:deep(.el-tabs__item:hover) {
  color: #f56c6c;
}

:deep(.el-tabs__item.is-active) {
  color: #f56c6c;
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #f56c6c 0%, #ff5757 100%);
  height: 3px;
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-table__header) {
  background: linear-gradient(135deg, #fff5f5 0%, #fffafa 100%);
}

:deep(.el-table th) {
  background: transparent;
  color: #606266;
  font-weight: 600;
  font-size: 14px;
}

:deep(.el-table tbody tr:hover) {
  background: linear-gradient(135deg, #fffafa 0%, #fff5f5 100%);
}

:deep(.el-button--warning) {
  background: linear-gradient(135deg, #e6a23c 0%, #f5ba3c 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--warning:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(230, 162, 60, 0.4);
}

:deep(.el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
}

:deep(.el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #ff5757 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(245, 108, 108, 0.4);
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  border: none;
  transition: all 0.3s ease;
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

:deep(.el-progress__text) {
  font-size: 12px !important;
  font-weight: 600;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 2px 12px rgba(245, 108, 108, 0.15);
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-tag) {
  border-radius: 6px;
  padding: 0 12px;
  font-weight: 500;
}

:deep(.el-dialog) {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #fff5f5 0%, #fffafa 100%);
  padding: 20px;
}

:deep(.el-dialog__title) {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
