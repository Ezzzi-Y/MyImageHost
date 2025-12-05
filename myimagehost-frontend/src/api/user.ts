import request from '@/utils/request'
import type {
  ApiResponse,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  ForgetPasswordRequest,
  Image,
  ImageListResponse,
  UpdateAliasRequest,
  DeleteImagesRequest
} from '@/types'

// 获取邮箱验证码
export const getVerificationCode = (email: string): Promise<ApiResponse<null>> => {
  return request.get('/auth/email/verification', { params: { email } })
}

// 用户注册
export const register = (data: RegisterRequest): Promise<ApiResponse<LoginResponse>> => {
  return request.post('/auth/register', data)
}

// 用户登录
export const login = (data: LoginRequest): Promise<ApiResponse<LoginResponse>> => {
  return request.post('/auth/login', data)
}

// 忘记密码
export const forgetPassword = (data: ForgetPasswordRequest): Promise<ApiResponse<null>> => {
  return request.post('/auth/forgetPassword', data)
}

// 上传图片
export const uploadImage = (formData: FormData): Promise<ApiResponse<null>> => {
  return request.post('/user/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 分页查询图片列表
export const listImages = (page: number, pageSize: number): Promise<ApiResponse<ImageListResponse>> => {
  return request.get('/user/listImage', { params: { page, pageSize } })
}

// 搜索图片
export const searchImages = (alias: string): Promise<ApiResponse<Image[]>> => {
  return request.get('/user/searchImages', { params: { alias } })
}

// 修改图片别名
export const updateAlias = (data: UpdateAliasRequest): Promise<ApiResponse<null>> => {
  return request.put('/user/updateAlias', data)
}

// 批量删除图片
export const deleteImageBatch = (data: DeleteImagesRequest): Promise<ApiResponse<null>> => {
  return request.delete('/user/deleteImageBatch', { data })
}

// 获取用户配额信息
export const getUserQuota = (): Promise<ApiResponse<{ quotaSpace: number; quotaCount: number; usedSpace: number; usedCount: number }>> => {
  return request.get('/user/quota')
}
