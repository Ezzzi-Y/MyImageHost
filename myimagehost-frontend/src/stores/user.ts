import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserQuota } from '@/api/user'
import type { LoginRequest, RegisterRequest, LoginResponse } from '@/types'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 初始化时从 localStorage 恢复
  const storedToken = localStorage.getItem('token')
  const storedUserInfo = localStorage.getItem('userInfo')
  
  const userInfo = ref<LoginResponse | null>(
    storedUserInfo ? JSON.parse(storedUserInfo) : null
  )
  const token = ref<string | null>(storedToken)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value && !!userInfo.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // 登录
  async function login(data: LoginRequest) {
    const res = await loginApi(data)
    userInfo.value = res.data
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    
    // 根据角色跳转到不同页面
    if (res.data.role === 'ADMIN') {
      router.push('/admin')
    } else {
      router.push('/dashboard')
    }
  }

  // 注册
  async function register(data: RegisterRequest) {
    const res = await registerApi(data)
    userInfo.value = res.data
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('userInfo', JSON.stringify(res.data))
    router.push('/dashboard')
  }

  // 登出
  function logout() {
    userInfo.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }

  // 更新用户信息（用于上传、删除后更新配额）
  function updateUserInfo(info: Partial<LoginResponse>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
    }
  }

  // 从 token 恢复用户信息（可以调用后端接口获取当前用户信息）
  function initFromToken() {
    const storedToken = localStorage.getItem('token')
    if (storedToken) {
      token.value = storedToken
      // 注意：这里需要后端提供获取当前用户信息的接口
      // 暂时从 localStorage 恢复
      const storedUser = localStorage.getItem('userInfo')
      if (storedUser) {
        userInfo.value = JSON.parse(storedUser)
      }
    }
  }

  // 保存用户信息到 localStorage
  function saveUserInfo() {
    if (userInfo.value) {
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  // 刷新用户配额信息
  async function refreshQuota() {
    if (!token.value) return
    
    try {
      const res = await getUserQuota()
      if (userInfo.value) {
        userInfo.value.quotaSpace = res.data.quotaSpace
        userInfo.value.quotaCount = res.data.quotaCount
        userInfo.value.usedSpace = res.data.usedSpace
        userInfo.value.usedCount = res.data.usedCount
        saveUserInfo()
      }
    } catch (error) {
      console.error('刷新配额信息失败:', error)
    }
  }

  return {
    userInfo,
    token,
    isLoggedIn,
    isAdmin,
    login,
    register,
    logout,
    updateUserInfo,
    initFromToken,
    saveUserInfo,
    refreshQuota
  }
})
