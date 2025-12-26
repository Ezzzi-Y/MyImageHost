// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 用户相关类型
export interface User {
  id?: number
  email: string
  nickname: string
  role: 'USER' | 'ADMIN'
  enabled?: boolean
  quotaSpace: number
  quotaCount: number
  usedSpace: number
  usedCount: number
  disabledUntil?: string | null
  disableReason?: string | null
  createTime?: string
  updateTime?: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  email: string
  nickname: string
  role: 'USER' | 'ADMIN'
  token: string
  quotaSpace: number
  quotaCount: number
  usedSpace: number
  usedCount: number
}

export interface RegisterRequest {
  nickname: string
  email: string
  password: string
  verificationCode: string
}

export interface ForgetPasswordRequest {
  email: string
  password: string
  verificationCode: string
}

// 图片相关类型
export interface Image {
  id: number
  userId: number
  originalName: string
  alias: string
  url: string
  md5: string
  size: number
  createTime: string
}

export interface ImageListResponse {
  total: number
  records: Image[]
}

export interface UpdateAliasRequest {
  id: number
  alias: string
}

export interface DeleteImagesRequest {
  ids: number[]
}

// 管理员相关类型
export interface DisableUserRequest {
  userId: number
  reason: string
  durationDays: number
}

export interface UpdateSpaceRequest {
  id: number
  quotaSpace: number
  quotaCount: number
}

export interface SearchUserRequest {
  email?: string
  nickname?: string
  enabled?: boolean
}

// 用户配额响应类型
export interface QuotaResponse {
  quotaSpace: number
  quotaCount: number
  usedSpace: number
  usedCount: number
}

// 系统测试信息类型
export interface TestInfoResponse {
  testStatus: boolean
  testMessage: string
}

export interface UpdateTestStatusRequest {
  testStatus: boolean
  testMessage: string
}

// 功能开关类型
export interface FeatureSwitch {
  featureName: string
  description: string
  enabled: boolean
}

export interface UpdateFeatureSwitchRequest {
  featureName: string
  enabled: boolean
}
