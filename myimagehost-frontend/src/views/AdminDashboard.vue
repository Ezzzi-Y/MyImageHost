<template>
  <div class="admin-wrapper">
    <el-container class="full-height-container">
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

      <el-main class="main-content">
        <div class="content-wrapper">
          <el-tabs v-model="activeTab">
            <el-tab-pane label="用户管理" name="users">
              <el-card shadow="hover">
                <el-row :gutter="20" style="margin-bottom: 20px">
                  <el-col :span="6">
                    <el-input v-model="searchForm.email" placeholder="邮箱" clearable />
                  </el-col>
                  <el-col :span="6">
                    <el-input v-model="searchForm.nickname" placeholder="昵称" clearable />
                  </el-col>
                  <el-col :span="6">
                    <el-select
                      v-model="searchForm.enabled"
                      placeholder="账号状态"
                      clearable
                      style="width: 100%"
                    >
                      <el-option label="启用" :value="true" />
                      <el-option label="禁用" :value="false" />
                    </el-select>
                  </el-col>
                  <el-col :span="6">
                    <el-button type="primary" @click="handleSearch">搜索</el-button>
                    <el-button @click="handleReset">重置</el-button>
                  </el-col>
                </el-row>

                <el-table :data="users" style="width: 100%" v-loading="loading" border>
                  <el-table-column prop="id" label="ID" width="60" align="center" />
                  <el-table-column
                    prop="email"
                    label="邮箱"
                    min-width="180"
                    show-overflow-tooltip
                  />
                  <el-table-column prop="nickname" label="昵称" width="120" />
                  <el-table-column prop="role" label="角色" width="100" align="center">
                    <template #default="scope">
                      <el-tag
                        :type="scope.row.role === 'ADMIN' ? 'danger' : 'primary'"
                        size="small"
                      >
                        {{ scope.row.role === 'ADMIN' ? '管理员' : '用户' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="enabled" label="状态" width="80" align="center">
                    <template #default="scope">
                      <el-tag :type="scope.row.enabled ? 'success' : 'danger'" size="small">
                        {{ scope.row.enabled ? '启用' : '禁用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="空间配额" width="160">
                    <template #default="scope">
                      <div class="quota-text">
                        {{ formatSize(scope.row.usedSpace) }} /
                        {{ formatSize(scope.row.quotaSpace) }}
                      </div>
                      <el-progress
                        :percentage="calculatePercentage(scope.row.usedSpace, scope.row.quotaSpace)"
                        :format="() => ''"
                        :stroke-width="6"
                      />
                    </template>
                  </el-table-column>
                  <el-table-column label="数量配额" width="120" align="center">
                    <template #default="scope">
                      <div>{{ scope.row.usedCount }} / {{ scope.row.quotaCount }}</div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="createTime" label="创建时间" width="170" />
                  <el-table-column label="封禁信息" width="120" align="center">
                    <template #default="scope">
                      <div v-if="!scope.row.enabled && scope.row.disabledUntil">
                        <el-tooltip
                          :content="`原因: ${scope.row.disableReason || '无'}`"
                          placement="top"
                        >
                          <el-tag type="warning" size="small">
                            {{ formatDate(scope.row.disabledUntil) }}到期
                          </el-tag>
                        </el-tooltip>
                      </div>
                      <span v-else-if="!scope.row.enabled" class="text-gray">永久禁用</span>
                      <span v-else class="text-success">正常</span>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="280" fixed="right">
                    <template #default="scope">
                      <el-button
                        v-if="!scope.row.enabled"
                        size="small"
                        link
                        type="info"
                        @click="viewBanDetail(scope.row)"
                        >详情</el-button
                      >
                      <el-button
                        size="small"
                        link
                        type="primary"
                        @click="openQuotaDialog(scope.row)"
                        >配额</el-button
                      >
                      <el-button
                        v-if="scope.row.enabled"
                        size="small"
                        link
                        type="warning"
                        @click="openDisableDialog(scope.row)"
                        >禁用</el-button
                      >
                      <el-button
                        v-else
                        size="small"
                        link
                        type="success"
                        @click="handleEnableUser(scope.row.id)"
                        >启用</el-button
                      >
                      <el-button
                        size="small"
                        link
                        type="danger"
                        @click="handleDeleteUser(scope.row.id)"
                        :disabled="scope.row.role === 'ADMIN'"
                        >删除</el-button
                      >
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="我的图床" name="images">
              <div class="image-bed-container">
                <el-card shadow="hover" class="mb-20">
                  <template #header>
                    <div class="card-header">
                      <span>存储配额</span>
                    </div>
                  </template>
                  <el-row :gutter="40">
                    <el-col :span="12">
                      <div class="quota-item">
                        <div class="quota-info">
                          <span class="label">空间使用</span>
                          <span class="value"
                            >{{ formatSize(userStore.userInfo?.usedSpace || 0) }} /
                            {{ formatSize(userStore.userInfo?.quotaSpace || 0) }}</span
                          >
                        </div>
                        <el-progress
                          :percentage="spacePercentage"
                          :color="progressColor"
                          :stroke-width="10"
                        />
                      </div>
                    </el-col>
                    <el-col :span="12">
                      <div class="quota-item">
                        <div class="quota-info">
                          <span class="label">文件数量</span>
                          <span class="value"
                            >{{ userStore.userInfo?.usedCount || 0 }} /
                            {{ userStore.userInfo?.quotaCount || 0 }}</span
                          >
                        </div>
                        <el-progress
                          :percentage="countPercentage"
                          :color="progressColor"
                          :stroke-width="10"
                        />
                      </div>
                    </el-col>
                  </el-row>
                </el-card>

                <el-card shadow="hover" class="mb-20">
                  <el-upload
                    class="upload-demo"
                    drag
                    action="#"
                    :http-request="customUpload"
                    :show-file-list="false"
                    :before-upload="beforeUpload"
                    accept="image/*"
                  >
                    <el-icon class="el-icon--upload" :size="50"><UploadFilled /></el-icon>
                    <div class="el-upload__text">拖拽图片到此处或<em>点击上传</em></div>
                  </el-upload>
                </el-card>

                <el-card shadow="hover" class="mb-20">
                  <div class="search-bar">
                    <el-input
                      v-model="imageSearchAlias"
                      placeholder="搜索图片别名"
                      clearable
                      @clear="handleImageSearch"
                      @keyup.enter="handleImageSearch"
                      style="max-width: 400px"
                    >
                      <template #append>
                        <el-button :icon="Search" @click="handleImageSearch" />
                      </template>
                    </el-input>
                    <el-button
                      type="danger"
                      :disabled="selectedMyImages.length === 0"
                      @click="handleBatchDeleteImages"
                    >
                      批量删除 ({{ selectedMyImages.length }})
                    </el-button>
                  </div>
                </el-card>

                <el-card shadow="hover">
                  <template #header>
                    <div class="card-header">
                      <span>我的图片 ({{ imagePagination.total }})</span>
                    </div>
                  </template>

                  <el-table
                    :data="myImages"
                    style="width: 100%"
                    v-loading="imageLoading"
                    @selection-change="handleImageSelectionChange"
                    border
                  >
                    <el-table-column type="selection" width="55" align="center" />
                    <el-table-column prop="alias" label="别名" width="150" show-overflow-tooltip />
                    <el-table-column label="预览" width="100" align="center">
                      <template #default="scope">
                        <el-image
                          style="width: 60px; height: 60px; border-radius: 4px"
                          :src="scope.row.url"
                          :preview-src-list="[scope.row.url]"
                          fit="cover"
                          preview-teleported
                        />
                      </template>
                    </el-table-column>
                    <el-table-column
                      prop="originalName"
                      label="原始文件名"
                      min-width="200"
                      show-overflow-tooltip
                    />
                    <el-table-column label="大小" width="100">
                      <template #default="scope">
                        {{ formatSize(scope.row.size) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="链接" min-width="200" show-overflow-tooltip>
                      <template #default="scope">
                        <span class="url-text" @click="copyImageUrl(scope.row.url)">{{
                          scope.row.url
                        }}</span>
                      </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="上传时间" width="170" />
                    <el-table-column label="操作" width="200" fixed="right">
                      <template #default="scope">
                        <el-button
                          size="small"
                          link
                          type="primary"
                          @click="copyImageUrl(scope.row.url)"
                          >复制</el-button
                        >
                        <el-button
                          size="small"
                          link
                          type="primary"
                          @click="handleEditImageAlias(scope.row)"
                          >改名</el-button
                        >
                        <el-button
                          size="small"
                          link
                          type="danger"
                          @click="handleDeleteImage(scope.row.id)"
                          >删除</el-button
                        >
                      </template>
                    </el-table-column>
                  </el-table>

                  <div class="pagination-container">
                    <el-pagination
                      v-model:current-page="imagePagination.page"
                      v-model:page-size="imagePagination.pageSize"
                      :page-sizes="[10, 20, 50, 100]"
                      :total="imagePagination.total"
                      layout="total, sizes, prev, pager, next, jumper"
                      @size-change="handleImageSizeChange"
                      @current-change="handleImagePageChange"
                      background
                    />
                  </div>
                </el-card>
              </div>
            </el-tab-pane>

            <el-tab-pane label="系统设置" name="settings">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>测试状态设置</span>
                  </div>
                </template>

                <el-form :model="testStatusForm" label-width="120px" style="max-width: 600px">
                  <el-form-item label="测试状态">
                    <el-switch
                      v-model="testStatusForm.testStatus"
                      active-text="启用"
                      inactive-text="关闭"
                    />
                  </el-form-item>
                  <el-form-item label="测试信息">
                    <el-input
                      v-model="testStatusForm.testMessage"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入测试信息提示文字"
                      maxlength="100"
                      show-word-limit
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-button
                      type="primary"
                      @click="handleUpdateTestStatus"
                      :loading="updatingTestStatus"
                    >
                      保存设置
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </el-tab-pane>

            <el-tab-pane label="功能管理" name="features">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>功能开关管理</span>
                    <el-button size="small" @click="loadFeatures">刷新</el-button>
                  </div>
                </template>

                <el-table :data="features" style="width: 100%" v-loading="loadingFeatures" border>
                  <el-table-column prop="featureName" label="功能标识" width="200" />
                  <el-table-column prop="description" label="功能描述" min-width="200" />
                  <el-table-column label="状态" width="120" align="center">
                    <template #default="scope">
                      <el-tag :type="scope.row.enabled ? 'success' : 'danger'" size="small">
                        {{ scope.row.enabled ? '启用' : '禁用' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120" align="center">
                    <template #default="scope">
                      <el-switch
                        v-model="scope.row.enabled"
                        @change="handleFeatureToggle(scope.row)"
                        :loading="scope.row.loading"
                      />
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-main>
    </el-container>

    <el-dialog v-model="quotaDialogVisible" title="修改用户配额" width="450px" append-to-body>
      <el-form :model="quotaForm" label-width="100px">
        <el-form-item label="用户邮箱">
          <el-input v-model="currentUser.email" disabled />
        </el-form-item>
        <el-form-item label="空间(MB)">
          <el-input-number v-model="quotaForm.spaceMB" :min="0" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数量配额">
          <el-input-number v-model="quotaForm.count" :min="0" :step="10" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="quotaDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveQuota">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="disableDialogVisible" title="禁用用户" width="450px" append-to-body>
      <el-form :model="disableForm" label-width="100px">
        <el-form-item label="禁用时长(天)">
          <el-input-number v-model="disableForm.durationDays" :min="1" :max="365" />
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="disableForm.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disableDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handleDisableUser">确定禁用</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="uploadDialogVisible" title="上传图片" width="400px" append-to-body>
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="别名">
          <el-input v-model="uploadForm.alias" placeholder="可选" />
        </el-form-item>
        <el-form-item label="预览">
          <el-image v-if="previewUrl" :src="previewUrl" fit="contain" style="max-height: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpload" :loading="uploadingImage">上传</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="imageAliasDialogVisible" title="修改别名" width="400px" append-to-body>
      <el-input v-model="imageAliasForm.alias" placeholder="请输入新别名" />
      <template #footer>
        <el-button @click="imageAliasDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUpdateImageAlias">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="banDetailVisible" title="封禁详情" width="400px" append-to-body>
      <div class="ban-detail-content">
        <p><strong>账号:</strong> {{ banDetail.email }}</p>
        <p><strong>原因:</strong> {{ banDetail.disableReason }}</p>
        <p><strong>解封时间:</strong> {{ banDetail.disabledUntil }}</p>
      </div>
      <template #footer>
        <el-button @click="banDetailVisible = false">关闭</el-button>
        <el-button type="success" @click="handleEnableUserFromDetail">解封</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
// Script 部分逻辑保持不变，可以直接复用原来的逻辑
// 为了节省篇幅，这里假设所有的 import 和 methods 都与原代码一致
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Search } from '@element-plus/icons-vue'
import {
  listUsers,
  disableUser,
  enableUser,
  deleteUser,
  updateSpace,
  searchUser,
  updateTestStatus,
  getTestInfo,
  getAllFeatures,
  updateFeatureSwitch,
} from '@/api/admin'
import { uploadImage, listImages, searchImages, updateAlias, deleteImageBatch } from '@/api/user'
import type { User, Image, FeatureSwitch } from '@/types'

// ... (此处保留你原有的所有 JS 逻辑，无需改动) ...

const userStore = useUserStore()
const activeTab = ref('users')
const users = ref<User[]>([])
const loading = ref(false)

// 图片管理相关
const myImages = ref<Image[]>([])
const imageLoading = ref(false)
const uploadingImage = ref(false)
const imageSearchAlias = ref('')
const selectedMyImages = ref<Image[]>([])
const imagePagination = reactive({ page: 1, pageSize: 10, total: 0 })

// 搜索表单
const searchForm = reactive({ email: '', nickname: '', enabled: undefined as boolean | undefined })

// 弹窗状态
const quotaDialogVisible = ref(false)
const currentUser = reactive({ id: 0, email: '' })
const quotaForm = reactive({ spaceMB: 100, count: 100 })
const disableDialogVisible = ref(false)
const disableForm = reactive({ durationDays: 7, reason: '' })
const banDetailVisible = ref(false)
const banDetail = reactive({ id: 0, email: '', nickname: '', disableReason: '', disabledUntil: '' })
const uploadDialogVisible = ref(false)
const currentFile = ref<File | null>(null)
const previewUrl = ref('')
const uploadForm = reactive({ alias: '' })
const imageAliasDialogVisible = ref(false)
const imageAliasForm = reactive({ id: 0, alias: '' })
const testStatusForm = reactive({ testStatus: false, testMessage: '' })
const updatingTestStatus = ref(false)

// 功能开关管理相关
const features = ref<FeatureSwitch[]>([])
const loadingFeatures = ref(false)

// 计算属性和工具函数复用原代码...
const spacePercentage = computed(() => {
  if (!userStore.userInfo) return 0
  return Math.min(
    100,
    Math.round((userStore.userInfo.usedSpace / userStore.userInfo.quotaSpace) * 100),
  )
})
const countPercentage = computed(() => {
  if (!userStore.userInfo) return 0
  return Math.min(
    100,
    Math.round((userStore.userInfo.usedCount / userStore.userInfo.quotaCount) * 100),
  )
})
const progressColor = (percentage: number) =>
  percentage < 60 ? '#67c23a' : percentage < 80 ? '#e6a23c' : '#f56c6c'
const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
const calculatePercentage = (used: number, total: number) =>
  !total ? 0 : Math.min(100, Math.round((used / total) * 100))
const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  return dateStr.split(' ')[0]
}
const calculateRemainingDays = (dateStr: string) => {
  if (!dateStr) return 0
  const targetDate = new Date(dateStr)
  const now = new Date()
  const diff = targetDate.getTime() - now.getTime()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

// 方法复用原代码...
const viewBanDetail = (user: User) => {
  banDetail.id = user.id!
  banDetail.email = user.email
  banDetail.nickname = user.nickname
  banDetail.disableReason = user.disableReason || ''
  banDetail.disabledUntil = user.disabledUntil || ''
  banDetailVisible.value = true
}
const handleEnableUserFromDetail = async () => {
  await enableUser(banDetail.id)
  banDetailVisible.value = false
  fetchUsers()
}
const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await listUsers()
    users.value = res.data
  } finally {
    loading.value = false
  }
}
const handleSearch = async () => {
  loading.value = true
  try {
    const res = await searchUser(searchForm)
    users.value = res.data
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    loading.value = false
  }
}
const handleReset = () => {
  searchForm.email = ''
  searchForm.nickname = ''
  searchForm.enabled = undefined
  fetchUsers()
}
const openQuotaDialog = (user: User) => {
  currentUser.id = user.id!
  currentUser.email = user.email
  quotaForm.spaceMB = Math.round(user.quotaSpace / 1024 / 1024)
  quotaForm.count = user.quotaCount
  quotaDialogVisible.value = true
}
const handleSaveQuota = async () => {
  try {
    await updateSpace({
      id: currentUser.id,
      quotaSpace: quotaForm.spaceMB * 1024 * 1024,
      quotaCount: quotaForm.count,
    })
    ElMessage.success('配额修改成功')
    quotaDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('修改配额失败:', error)
  }
}
const openDisableDialog = (user: User) => {
  currentUser.id = user.id!
  currentUser.email = user.email
  disableForm.durationDays = 7
  disableForm.reason = ''
  disableDialogVisible.value = true
}
const handleDisableUser = async () => {
  try {
    await disableUser({
      userId: currentUser.id,
      reason: disableForm.reason,
      durationDays: disableForm.durationDays,
    })
    ElMessage.success('用户已禁用')
    disableDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    console.error('禁用用户失败:', error)
  }
}
const handleEnableUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要启用该用户吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
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
const handleDeleteUser = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗?此操作不可恢复!', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error',
    })
    await deleteUser(id)
    ElMessage.success('用户已删除')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
    }
  }
}
const handleLogout = () => userStore.logout()
const fetchMyImages = async () => {
  imageLoading.value = true
  try {
    const res = await listImages(imagePagination.page, imagePagination.pageSize)
    myImages.value = res.data.records
    imagePagination.total = res.data.total
  } catch (error) {
    console.error('获取图片列表失败:', error)
  } finally {
    imageLoading.value = false
  }
}
const handleImageSearch = async () => {
  if (!imageSearchAlias.value.trim()) {
    fetchMyImages()
    return
  }
  imageLoading.value = true
  try {
    const res = await searchImages(imageSearchAlias.value)
    myImages.value = res.data
    imagePagination.total = res.data.length
  } catch (error) {
    console.error('搜索图片失败:', error)
  } finally {
    imageLoading.value = false
  }
}
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}
const customUpload = (options: any) => {
  currentFile.value = options.file
  uploadForm.alias = options.file.name.split('.')[0]
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(options.file)
  uploadDialogVisible.value = true
}
const confirmUpload = async () => {
  if (!uploadForm.alias.trim()) {
    ElMessage.warning('请输入图片别名')
    return
  }
  if (!currentFile.value) return
  try {
    uploadingImage.value = true
    const formData = new FormData()
    formData.append('file', currentFile.value)
    formData.append('alias', uploadForm.alias)
    await uploadImage(formData)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    uploadForm.alias = ''
    previewUrl.value = ''
    currentFile.value = null
    fetchMyImages()
    userStore.refreshQuota()
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploadingImage.value = false
  }
}
const copyImageUrl = (url: string) => {
  navigator.clipboard.writeText(url)
  ElMessage.success('已复制')
}
const handleEditImageAlias = (image: Image) => {
  imageAliasForm.id = image.id
  imageAliasForm.alias = image.alias
  imageAliasDialogVisible.value = true
}
const confirmUpdateImageAlias = async () => {
  if (!imageAliasForm.alias.trim()) {
    ElMessage.warning('请输入新别名')
    return
  }
  try {
    await updateAlias({ id: imageAliasForm.id, alias: imageAliasForm.alias })
    ElMessage.success('修改成功')
    imageAliasDialogVisible.value = false
    fetchMyImages()
  } catch (error) {
    console.error('修改别名失败:', error)
  }
}
const handleDeleteImage = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteImageBatch({ ids: [id] })
    ElMessage.success('删除成功')
    fetchMyImages()
    userStore.refreshQuota()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}
const handleBatchDeleteImages = async () => {
  if (selectedMyImages.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedMyImages.value.length} 张图片吗?`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )
    const ids = selectedMyImages.value.map((img) => img.id)
    await deleteImageBatch({ ids })
    ElMessage.success('删除成功')
    selectedMyImages.value = []
    fetchMyImages()
    userStore.refreshQuota()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}
const handleImageSelectionChange = (selection: Image[]) => (selectedMyImages.value = selection)
const handleImagePageChange = (page: number) => {
  imagePagination.page = page
  fetchMyImages()
}
const handleImageSizeChange = (size: number) => {
  imagePagination.pageSize = size
  fetchMyImages()
}
const loadTestInfo = async () => {
  try {
    const response = await getTestInfo()
    if (response.code === 1 && response.data) {
      testStatusForm.testStatus = response.data.testStatus
      testStatusForm.testMessage = response.data.testMessage
    }
  } catch (error: any) {
    ElMessage.error('加载测试状态失败')
  }
}
const handleUpdateTestStatus = async () => {
  updatingTestStatus.value = true
  try {
    const response = await updateTestStatus({
      testStatus: testStatusForm.testStatus,
      testMessage: testStatusForm.testMessage,
    })
    if (response.code === 1) {
      ElMessage.success('测试状态更新成功')
    } else {
      ElMessage.error(response.message || '更新失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '更新测试状态失败')
  } finally {
    updatingTestStatus.value = false
  }
}

// 功能开关管理相关方法
const loadFeatures = async () => {
  loadingFeatures.value = true
  try {
    const response = await getAllFeatures()
    if (response.code === 1) {
      features.value = response.data || []
    } else {
      ElMessage.error(response.message || '加载功能列表失败')
    }
  } catch (error: any) {
    ElMessage.error('加载功能列表失败')
  } finally {
    loadingFeatures.value = false
  }
}

const handleFeatureToggle = async (feature: FeatureSwitch) => {
  try {
    const response = await updateFeatureSwitch({
      featureName: feature.featureName,
      enabled: feature.enabled,
    })
    if (response.code === 1) {
      ElMessage.success(`功能【${feature.description}】已${feature.enabled ? '启用' : '禁用'}`)
    } else {
      ElMessage.error(response.message || '更新失败')
      // 失败时回滚状态
      feature.enabled = !feature.enabled
    }
  } catch (error: any) {
    ElMessage.error('更新功能开关失败')
    // 失败时回滚状态
    feature.enabled = !feature.enabled
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'images' && myImages.value.length === 0) fetchMyImages()
  if (newTab === 'settings') loadTestInfo()
  if (newTab === 'features' && features.value.length === 0) loadFeatures()
})
onMounted(() => {
  fetchUsers()
  if (activeTab.value === 'images') fetchMyImages()
  if (activeTab.value === 'settings') loadTestInfo()
  if (activeTab.value === 'features') loadFeatures()
})
</script>

<style scoped>
/* 核心修复部分 
*/
.admin-wrapper {
  height: 100vh; /* 撑满视口高度 */
  overflow: hidden; /* 防止 body 出现滚动条 */
  background: var(--color-bg-layout, #f5f7fa);
}

.full-height-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.header {
  flex-shrink: 0; /* 头部不压缩 */
  background: #fff;
  border-bottom: 1px solid #dcdfe6;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  z-index: 10;
}

.main-content {
  flex: 1;
  overflow-y: auto; /* 仅在内容区域出现滚动条 */
  padding: 20px;

  /* 关键修复：scrollbar-gutter
    这会保留滚动条的空间（即是内容没有溢出），
    从而保证内容区域的宽度在有无滚动条时保持一致。
  */
  scrollbar-gutter: stable;
}

/* 限制内容最大宽度，进一步增强稳定性并优化大屏体验 */
.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
}

/* 其他样式优化 */
.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  font-size: 14px;
  color: #606266;
}

.mb-20 {
  margin-bottom: 20px;
}

.quota-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.quota-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #606266;
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.url-text {
  cursor: pointer;
  color: var(--el-color-primary);
}

.url-text:hover {
  text-decoration: underline;
}

.text-gray {
  color: #909399;
  font-size: 12px;
}
.text-success {
  color: #67c23a;
  font-size: 12px;
}
</style>
