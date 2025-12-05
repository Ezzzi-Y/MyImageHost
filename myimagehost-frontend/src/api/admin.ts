import request from '@/utils/request'
import type {
  ApiResponse,
  User,
  DisableUserRequest,
  UpdateSpaceRequest,
  SearchUserRequest
} from '@/types'

// 查询用户列表
export const listUsers = (): Promise<ApiResponse<User[]>> => {
  return request.get('/admin/list')
}

// 禁用用户
export const disableUser = (data: DisableUserRequest): Promise<ApiResponse<null>> => {
  return request.post('/admin/disableUser', data)
}

// 启用用户
export const enableUser = (id: number): Promise<ApiResponse<null>> => {
  return request.put(`/admin/enableUser/${id}`)
}

// 删除用户
export const deleteUser = (id: number): Promise<ApiResponse<null>> => {
  return request.delete(`/admin/deleteUser/${id}`)
}

// 修改用户空间配额
export const updateSpace = (data: UpdateSpaceRequest): Promise<ApiResponse<null>> => {
  return request.post('/admin/updateSpace', data)
}

// 搜索用户
export const searchUser = (data: SearchUserRequest): Promise<ApiResponse<User[]>> => {
  return request.post('/admin/searchUser', data)
}

// 根据ID获取用户
export const getUser = (id: number): Promise<ApiResponse<User>> => {
  return request.get(`/admin/getUser/${id}`)
}
