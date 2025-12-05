# 图床系统前端

基于 Vue 3 + TypeScript + Element Plus 开发的图床管理系统前端。

## 功能特性

### 用户功能
- 用户注册、登录、忘记密码
- 图片上传（拖拽上传、点击上传）
- 图片列表展示（分页）
- 图片搜索（按别名）
- 图片别名修改
- 批量删除图片
- 复制图片链接
- 存储配额实时显示

### 管理员功能
- 用户列表管理
- 用户搜索（邮箱、昵称、状态）
- 修改用户空间配额
- 禁用/启用用户
- 删除用户

## 技术栈

- Vue 3.5
- TypeScript
- Vite 7
- Vue Router 4
- Pinia (状态管理)
- Element Plus (UI 组件库)
- Axios (HTTP 请求)
- Spark-MD5 (文件 MD5 计算)

## 项目结构

```
src/
├── api/              # API 接口封装
│   ├── admin.ts     # 管理员接口
│   └── user.ts      # 用户接口
├── assets/          # 静态资源
├── components/      # 公共组件
├── router/          # 路由配置
├── stores/          # 状态管理
│   └── user.ts     # 用户状态
├── types/           # TypeScript 类型定义
│   └── index.ts
├── utils/           # 工具函数
│   └── request.ts  # Axios 封装
├── views/           # 页面组件
│   ├── LoginView.vue
│   ├── RegisterView.vue
│   ├── ResetPasswordView.vue
│   ├── UserDashboard.vue
│   └── AdminDashboard.vue
├── App.vue
└── main.ts
```

## 安装依赖

```bash
npm install
```

## 开发运行

```bash
npm run dev
```

项目将在 http://localhost:5173 运行，API 请求会自动代理到 http://localhost:8080

## 构建生产版本

```bash
npm run build
```

## 环境配置

### 开发环境
- 前端端口: 5173
- 后端 API: http://localhost:8080
- 自动代理配置已在 `vite.config.ts` 中设置

### API 代理配置

在 `vite.config.ts` 中配置了开发环境的 API 代理:

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

所有以 `/api` 开头的请求会被代理到后端服务器。

## 路由说明

- `/` - 重定向到登录页
- `/login` - 登录页面
- `/register` - 注册页面
- `/reset-password` - 重置密码页面
- `/dashboard` - 用户仪表盘（需要登录）
- `/admin` - 管理员后台（需要管理员权限）

## 状态管理

使用 Pinia 管理全局状态:

- `userStore` - 用户信息、登录状态、Token 管理

## 主要功能实现

### 文件上传
- 支持拖拽上传和点击选择文件
- 上传前校验文件类型和大小
- 可为图片添加别名
- 上传后自动刷新列表和配额信息

### 权限控制
- 路由守卫验证登录状态
- 管理员页面需要管理员角色
- Token 自动附加到请求头

### 错误处理
- Axios 拦截器统一处理错误
- 401/403 自动跳转登录页
- 友好的错误提示

## 注意事项

1. 确保后端服务已启动在 8080 端口
2. 首次运行需要安装依赖
3. Token 存储在 localStorage 中
4. 图片上传大小限制为 10MB
5. 验证码有效期为 60 秒

## 接口文档

详见项目根目录的 `图床接口.json` 文件。

## License

MIT
