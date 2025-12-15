<template>
  <div class="dashboard-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>图床系统</h2>
          <div class="user-info">
            <span class="nickname">{{ userStore.userInfo?.nickname }}</span>
            <span class="email">{{ userStore.userInfo?.email }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出</el-button>
          </div>
        </div>
      </el-header>

      <el-main>
        <!-- 配额信息 -->
        <el-card class="quota-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>存储配额</span>
            </div>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="quota-item">
                <div class="quota-label">空间使用</div>
                <div class="quota-value">
                  {{ formatSize(userStore.userInfo?.usedSpace || 0) }} /
                  {{ formatSize(userStore.userInfo?.quotaSpace || 0) }}
                </div>
                <el-progress
                  :percentage="spacePercentage"
                  :color="progressColor(spacePercentage)"
                />
              </div>
            </el-col>
            <el-col :span="12">
              <div class="quota-item">
                <div class="quota-label">文件数量</div>
                <div class="quota-value">
                  {{ userStore.userInfo?.usedCount || 0 }} /
                  {{ userStore.userInfo?.quotaCount || 0 }}
                </div>
                <el-progress
                  :percentage="countPercentage"
                  :color="progressColor(countPercentage)"
                />
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 上传区域 -->
        <el-card class="upload-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>上传图片</span>
            </div>
          </template>
          <el-upload
            class="upload-demo"
            drag
            action="#"
            :http-request="customUpload"
            :show-file-list="false"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <el-icon class="el-icon--upload" :size="67">
              <UploadFilled />
            </el-icon>
            <div class="el-upload__text">拖拽图片到此处或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/png/gif 等格式</div>
            </template>
          </el-upload>
        </el-card>

        <!-- 搜索和操作区域 -->
        <el-card class="search-card" shadow="hover">
          <el-row :gutter="20">
            <el-col :span="18">
              <el-input
                v-model="searchAlias"
                placeholder="搜索图片别名"
                clearable
                @clear="handleSearch"
              >
                <template #append>
                  <el-button :icon="Search" @click="handleSearch" />
                </template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-button
                type="danger"
                :disabled="selectedImages.length === 0"
                @click="handleBatchDelete"
              >
                批量删除 ({{ selectedImages.length }})
              </el-button>
            </el-col>
          </el-row>
        </el-card>

        <!-- 图片列表 -->
        <el-card class="images-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>我的图片 ({{ pagination.total }})</span>
            </div>
          </template>

          <el-table
            :data="images"
            style="width: 100%"
            v-loading="loading"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column prop="alias" label="别名" width="150" />
            <el-table-column label="预览" width="120">
              <template #default="scope">
                <el-image
                  style="width: 80px; height: 80px"
                  :src="scope.row.url"
                  :preview-src-list="[scope.row.url]"
                  fit="cover"
                />
              </template>
            </el-table-column>
            <el-table-column
              prop="originalName"
              label="原始文件名"
              width="200"
              show-overflow-tooltip
            />
            <el-table-column label="文件大小" width="120">
              <template #default="scope">
                {{ formatSize(scope.row.size) }}
              </template>
            </el-table-column>
            <el-table-column label="URL" min-width="200" show-overflow-tooltip>
              <template #default="scope">
                <el-link :href="scope.row.url" target="_blank" type="primary">
                  {{ scope.row.url }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="上传时间" width="180" />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="scope">
                <el-button size="small" type="primary" @click="copyUrl(scope.row.url)">
                  复制链接
                </el-button>
                <el-button size="small" @click="handleEditAlias(scope.row)"> 修改别名 </el-button>
                <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div class="pagination-container">
            <el-pagination
              v-model:current-page="pagination.page"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </el-card>
      </el-main>
    </el-container>

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传图片" width="500px">
      <el-form :model="uploadForm" label-width="80px">
        <el-form-item label="图片别名">
          <el-input
            v-model="uploadForm.alias"
            placeholder="请输入图片别名（方便后续查找）"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="预览">
          <el-image
            v-if="previewUrl"
            style="width: 100%; max-height: 300px"
            :src="previewUrl"
            fit="contain"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload" :loading="uploading"> 上传 </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改别名对话框 -->
    <el-dialog v-model="aliasDialogVisible" title="修改别名" width="500px">
      <el-form :model="aliasForm" label-width="80px">
        <el-form-item label="新别名">
          <el-input
            v-model="aliasForm.alias"
            placeholder="请输入新的别名"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="aliasDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmUpdateAlias"> 确定 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, reactive } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Search } from '@element-plus/icons-vue'
import { uploadImage, listImages, searchImages, updateAlias, deleteImageBatch } from '@/api/user'
import type { Image } from '@/types'

const userStore = useUserStore()
const images = ref<Image[]>([])
const loading = ref(false)
const uploading = ref(false)
const searchAlias = ref('')
const selectedImages = ref<Image[]>([])

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0,
})

// 上传相关
const uploadDialogVisible = ref(false)
const currentFile = ref<File | null>(null)
const previewUrl = ref('')
const uploadForm = reactive({
  alias: '',
})

// 修改别名相关
const aliasDialogVisible = ref(false)
const aliasForm = reactive({
  id: 0,
  alias: '',
})

// 计算属性
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

// 工具函数
const formatSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const progressColor = (percentage: number) => {
  if (percentage < 60) return '#67c23a'
  if (percentage < 80) return '#e6a23c'
  return '#f56c6c'
}

// 获取图片列表
const fetchImages = async () => {
  try {
    loading.value = true
    const res = await listImages(pagination.page, pagination.pageSize)
    images.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取图片列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索图片
const handleSearch = async () => {
  if (!searchAlias.value.trim()) {
    fetchImages()
    return
  }

  try {
    loading.value = true
    const res = await searchImages(searchAlias.value)
    images.value = res.data
    pagination.total = res.data.length
  } catch (error) {
    console.error('搜索图片失败:', error)
  } finally {
    loading.value = false
  }
}

// 上传前校验
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

// 自定义上传
const customUpload = (options: any) => {
  currentFile.value = options.file
  uploadForm.alias = options.file.name.split('.')[0] // 默认使用文件名作为别名

  // 生成预览
  const reader = new FileReader()
  reader.onload = (e) => {
    previewUrl.value = e.target?.result as string
  }
  reader.readAsDataURL(options.file)

  uploadDialogVisible.value = true
}

// 确认上传
const confirmUpload = async () => {
  if (!uploadForm.alias.trim()) {
    ElMessage.warning('请输入图片别名')
    return
  }

  if (!currentFile.value) return

  try {
    uploading.value = true
    const formData = new FormData()
    formData.append('file', currentFile.value)
    formData.append('alias', uploadForm.alias)

    await uploadImage(formData)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false

    // 刷新列表和用户信息
    fetchImages()
    userStore.refreshQuota() // 更新配额信息
  } catch (error) {
    console.error('上传失败:', error)
  } finally {
    uploading.value = false
  }
}

// 复制链接
const copyUrl = (url: string) => {
  navigator.clipboard
    .writeText(url)
    .then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
    .catch(() => {
      ElMessage.error('复制失败，请手动复制')
    })
}

// 修改别名
const handleEditAlias = (image: Image) => {
  aliasForm.id = image.id
  aliasForm.alias = image.alias
  aliasDialogVisible.value = true
}

const confirmUpdateAlias = async () => {
  if (!aliasForm.alias.trim()) {
    ElMessage.warning('请输入新别名')
    return
  }

  try {
    await updateAlias({ id: aliasForm.id, alias: aliasForm.alias })
    ElMessage.success('修改成功')
    aliasDialogVisible.value = false
    fetchImages()
  } catch (error) {
    console.error('修改别名失败:', error)
  }
}

// 删除单张图片
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这张图片吗?', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteImageBatch({ ids: [id] })
    ElMessage.success('删除成功')
    fetchImages()
    userStore.refreshQuota() // 更新配额信息
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedImages.value.length === 0) return

  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedImages.value.length} 张图片吗?`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      },
    )

    const ids = selectedImages.value.map((img) => img.id)
    await deleteImageBatch({ ids })
    ElMessage.success('删除成功')
    fetchImages()
    userStore.refreshQuota() // 更新配额信息
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
    }
  }
}

// 表格选择
const handleSelectionChange = (selection: Image[]) => {
  selectedImages.value = selection
}

// 分页
const handlePageChange = (page: number) => {
  pagination.page = page
  fetchImages()
}

const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchImages()
}

// 登出
const handleLogout = () => {
  userStore.logout()
}

onMounted(() => {
  fetchImages()
  userStore.refreshQuota() // 初始化时刷新配额信息
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: var(--color-bg-layout);
}

.header {
  background: var(--color-bg-container);
  border-bottom: 1px solid var(--color-border-lighter);
  box-shadow: var(--color-shadow-sm);
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
  color: var(--color-text-base);
  font-size: 20px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
}

.nickname {
  font-weight: 600;
  color: var(--color-text-base);
  font-size: 14px;
}

.email {
  color: var(--color-text-secondary);
  font-size: 13px;
}

.el-main {
  padding: var(--spacing-lg);
  max-width: 1400px;
  margin: 0 auto;
  overflow-y: auto;
  height: calc(100vh - 60px);
}

.quota-card,
.upload-card,
.search-card,
.images-card {
  margin-bottom: var(--spacing-lg);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-lighter);
  box-shadow: var(--color-shadow-sm);
  transition: all var(--transition-base);
}

.quota-card:hover,
.upload-card:hover,
.search-card:hover,
.images-card:hover {
  box-shadow: var(--color-shadow-md);
  border-color: var(--color-border-light);
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-base);
  display: flex;
  align-items: center;
  gap: var(--spacing-sm);
}

.card-header::before {
  content: '';
  width: 3px;
  height: 16px;
  background: var(--color-primary);
  border-radius: 2px;
}

.quota-item {
  padding: var(--spacing-md);
  background: var(--color-bg-container);
  border: 1px solid var(--color-border-lighter);
  border-radius: var(--radius-md);
  transition: all var(--transition-base);
}

.quota-item:hover {
  border-color: var(--primary-200);
  background: var(--primary-50);
}

.quota-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-sm);
  font-weight: 500;
}

.quota-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-base);
  margin-bottom: 12px;
}

.upload-demo {
  width: 100%;
}

:deep(.el-upload-dragger) {
  border-radius: var(--radius-lg);
  border: 2px dashed var(--color-border-base);
  transition: all var(--transition-base);
  background: var(--color-bg-container);
}

:deep(.el-upload-dragger:hover) {
  border-color: var(--color-primary);
  background: var(--primary-50);
}

.el-icon--upload {
  font-size: 64px;
  color: var(--color-primary);
  margin: 40px 0 16px;
}

.el-upload__text {
  font-size: 14px;
  color: var(--color-text-base);
}

.el-upload__text em {
  color: var(--color-primary);
  font-style: normal;
  font-weight: 600;
}

.el-upload__tip {
  color: var(--color-text-secondary);
  font-size: 12px;
  margin-top: 8px;
}

.pagination-container {
  margin-top: var(--spacing-lg);
  display: flex;
  justify-content: flex-end;
}

:deep(.el-button--danger) {
  background: var(--error);
  border: none;
  transition: all var(--transition-base);
  color: #fff;
}

:deep(.el-button--danger:hover) {
  background: #ff7875;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

:deep(.el-button--primary) {
  background: var(--color-primary);
  border: none;
  transition: all var(--transition-base);
  color: #fff;
}

:deep(.el-button--primary:hover) {
  background: var(--color-primary-light);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

:deep(.el-button) {
  border-radius: var(--radius-md);
  font-weight: 500;
}

:deep(.el-table) {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-lighter);
}

:deep(.el-table__header) {
  background: var(--gray-50);
}

:deep(.el-table th) {
  background: transparent;
  color: var(--color-text-base);
  font-weight: 600;
  font-size: 14px;
  border-bottom: 1px solid var(--color-border-lighter);
}

:deep(.el-table td) {
  border-bottom: 1px solid var(--color-border-lighter);
}

:deep(.el-table tbody tr) {
  transition: all var(--transition-fast);
}

:deep(.el-table tbody tr:hover) {
  background: var(--gray-50);
}

:deep(.el-image) {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border-lighter);
  transition: all var(--transition-base);
}

:deep(.el-image:hover) {
  transform: scale(1.05);
  box-shadow: var(--color-shadow-md);
}

:deep(.el-progress__text) {
  font-size: 12px !important;
  font-weight: 600;
}

:deep(.el-progress .el-progress-bar__outer) {
  background-color: var(--gray-200);
}

:deep(.el-card__header) {
  border-bottom: 1px solid var(--color-border-lighter);
  padding: 18px 20px;
  background: var(--color-bg-container);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-base);
  box-shadow: none;
  transition: all var(--transition-base);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-light);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--primary-100);
}

:deep(.el-link) {
  font-size: 14px;
}

:deep(.el-pagination) {
  font-weight: 500;
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled).is-active) {
  background-color: var(--color-primary);
}

:deep(.el-pagination.is-background .el-pager li:not(.is-disabled):hover) {
  color: var(--color-primary);
}
</style>
