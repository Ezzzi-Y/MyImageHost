<template>
  <div class="login-container">
    <el-card class="box-card">
      <h2 class="title">图床系统 - 登录</h2>

      <!-- 测试信息提示 -->
      <el-alert
        v-if="testInfo.testStatus"
        :title="testInfo.testMessage"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%">
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="links">
            <el-button link @click="$router.push('/register')">注册账号</el-button>
            <el-button link @click="$router.push('/reset-password')">忘记密码?</el-button>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getTestInfo } from '@/api/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  email: '',
  password: '',
})

const testInfo = reactive({
  testStatus: false,
  testMessage: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await userStore.login(form)
        ElMessage.success('登录成功')
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取测试信息
const fetchTestInfo = async () => {
  try {
    const res = await getTestInfo()
    testInfo.testStatus = res.data.testStatus
    testInfo.testMessage = res.data.testMessage
  } catch (error) {
    console.error('获取测试信息失败:', error)
  }
}

onMounted(() => {
  fetchTestInfo()
})
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--color-bg-layout);
  padding: var(--spacing-lg);
}

.box-card {
  width: 420px;
  max-width: 100%;
  padding: var(--spacing-2xl) var(--spacing-xl);
  border-radius: var(--radius-lg);
  box-shadow: var(--color-shadow-md);
  background: var(--color-bg-container);
  border: 1px solid var(--color-border-lighter);
}

:deep(.el-card__body) {
  padding: 0;
}

.title {
  text-align: center;
  margin-bottom: var(--spacing-xl);
  color: var(--color-text-base);
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.links {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: var(--spacing-lg);
}

:deep(.el-form-item__label) {
  color: var(--color-text-base);
  font-weight: 500;
  font-size: 14px;
}

:deep(.el-button--primary) {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-md);
  background: var(--color-primary);
  border: none;
  transition: all var(--transition-base);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.15);
}

:deep(.el-button--primary:hover) {
  background: var(--color-primary-light);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.25);
}

:deep(.el-button--primary:active) {
  background: var(--color-primary-dark);
}

:deep(.el-button.is-link) {
  color: var(--color-primary);
  font-size: 14px;
  padding: 0;
  height: auto;
}

:deep(.el-button.is-link:hover) {
  color: var(--color-primary-light);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border-base);
  box-shadow: none;
  transition: all var(--transition-base);
  padding: 8px 12px;
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-primary-light);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px var(--primary-100);
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: var(--color-text-base);
}

:deep(.el-input__inner::placeholder) {
  color: var(--color-text-placeholder);
}
</style>
