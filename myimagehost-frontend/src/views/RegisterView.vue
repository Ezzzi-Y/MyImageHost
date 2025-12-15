<template>
  <div class="register-container">
    <el-card class="box-card">
      <h2 class="title">图床系统 - 注册</h2>

      <!-- 测试信息提示 -->
      <el-alert
        v-if="testInfo.testStatus"
        :title="testInfo.testMessage"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px"
      />

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱">
            <template #append>
              <el-button @click="sendCode" :disabled="cooldown > 0" :loading="sendingCode">
                {{ cooldown > 0 ? `${cooldown}s` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <el-input v-model="form.verificationCode" placeholder="请输入验证码" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%">
            注册
          </el-button>
        </el-form-item>
        <el-form-item>
          <div class="links">
            <el-button link @click="$router.push('/login')">已有账号？去登录</el-button>
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
import { getVerificationCode, getTestInfo } from '@/api/user'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const sendingCode = ref(false)
const cooldown = ref(0)

const form = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: '',
})

const testInfo = reactive({
  testStatus: false,
  testMessage: '',
})

const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (form.confirmPassword !== '') {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度在2-20个字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度为6位', trigger: 'blur' },
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
  confirmPassword: [{ required: true, validator: validatePass2, trigger: 'blur' }],
}

const sendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  // 验证邮箱格式
  const emailRule = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRule.test(form.email)) {
    ElMessage.warning('请输入正确的邮箱格式')
    return
  }

  try {
    sendingCode.value = true
    await getVerificationCode(form.email)
    ElMessage.success('验证码已发送')

    // 开始倒计时
    cooldown.value = 120
    const timer = setInterval(() => {
      cooldown.value--
      if (cooldown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  } finally {
    sendingCode.value = false
  }
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        await userStore.register({
          nickname: form.nickname,
          email: form.email,
          password: form.password,
          verificationCode: form.verificationCode,
        })
        ElMessage.success('注册成功')
      } catch (error) {
        console.error('注册失败:', error)
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--color-bg-layout);
  padding: var(--spacing-lg);
}

.box-card {
  width: 480px;
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
  justify-content: center;
  width: 100%;
  margin-top: 8px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
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

:deep(.el-input-group__append) {
  background: var(--color-bg-container);
  border: none;
  border-left: 1px solid var(--color-border-base);
  padding: 0;
  overflow: hidden;
}

:deep(.el-input-group__append .el-button) {
  color: var(--color-primary);
  font-size: 13px;
  padding: 0 20px;
  height: 100%;
  min-height: 38px;
  min-width: 110px;
  border: none;
  background: transparent;
  border-radius: 0;
  white-space: nowrap;
}

:deep(.el-input-group__append .el-button:hover) {
  color: var(--color-primary-light);
  background: var(--primary-50);
}

:deep(.el-input-group__append .el-button.is-disabled) {
  color: var(--color-text-placeholder);
}

:deep(.el-input__inner) {
  font-size: 14px;
  color: var(--color-text-base);
}

:deep(.el-input__inner::placeholder) {
  color: var(--color-text-placeholder);
}

:deep(.el-input-group__append .el-button) {
  border-radius: 0 8px 8px 0;
}
</style>
