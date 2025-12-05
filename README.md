# 🖼️ MyImageHost - 现代化图床系统

<div align="center">

![Version](https://img.shields.io/badge/version-0.0.1-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.10-brightgreen.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.25-green.svg)
![License](https://img.shields.io/badge/license-MIT-orange.svg)

一个功能完善、界面美观的现代化图床系统，支持图片上传、管理、分享等功能。

[功能特性](#-功能特性) • [技术栈](#-技术栈) • [快速开始](#-快速开始) • [API文档](#-api文档) • [截图预览](#-截图预览)

</div>

---

## ✨ 功能特性

### 👤 用户功能
- **图片上传**：支持拖拽上传，自动生成图片链接
- **图片管理**：图片列表查看、搜索、批量删除
- **别名管理**：为图片设置自定义别名，方便记忆和查找
- **配额显示**：实时显示空间使用情况和文件数量
- **快速复制**：一键复制图片链接到剪贴板
- **图片预览**：支持在线预览和查看原图

### 🔐 账户安全
- **邮箱验证**：注册和重置密码需要邮箱验证码
- **密码加密**：使用安全的密码加密存储
- **Token认证**：基于 Sa-Token 的身份验证机制
- **权限控制**：用户和管理员角色分离

### 🛡️ 管理员功能
- **用户管理**：查看、搜索、启用/禁用用户账号
- **配额管理**：调整用户存储空间和文件数量配额
- **账号封禁**：支持临时或永久封禁用户账号
- **用户详情**：查看用户详细信息和使用情况

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 3.4.10
- **数据库**: MySQL 8.0 + MyBatis-Plus 3.5.7
- **安全**: Sa-Token 1.37.0
- **工具库**: Hutool 5.8.25, Lombok
- **邮件服务**: Spring Boot Mail
- **JSON处理**: FastJSON 1.2.83

### 前端技术
- **框架**: Vue 3.5.25 + TypeScript
- **UI组件**: Element Plus 2.11.9
- **状态管理**: Pinia 3.0.4
- **路由**: Vue Router 4.6.3
- **HTTP客户端**: Axios 1.13.2
- **构建工具**: Vite 6.0.2
- **文件处理**: SparkMD5 3.0.2

## 📦 项目结构

```
MyImageHost/
├── myimagehost-backend/          # 后端项目
│   ├── src/main/java/
│   │   └── me/ezzi/myimagehostbackend/
│   │       ├── common/           # 公共类（常量、工具类）
│   │       ├── config/           # 配置类
│   │       ├── controller/       # 控制器
│   │       ├── exception/        # 异常处理
│   │       ├── handler/          # 处理器
│   │       ├── mapper/           # MyBatis Mapper
│   │       ├── pojo/             # 实体类（DTO、Entity、VO）
│   │       ├── properties/       # 配置属性
│   │       ├── satoken/          # Sa-Token配置
│   │       ├── service/          # 业务逻辑
│   │       └── task/             # 定时任务
│   └── src/main/resources/
│       ├── application.yml       # 应用配置
│       ├── mapper/               # MyBatis XML映射文件
│       └── templates/            # 邮件模板
├── myimagehost-frontend/         # 前端项目
│   ├── src/
│   │   ├── api/                  # API接口定义
│   │   ├── assets/               # 静态资源
│   │   ├── components/           # 公共组件
│   │   ├── router/               # 路由配置
│   │   ├── stores/               # 状态管理
│   │   ├── types/                # TypeScript类型定义
│   │   ├── utils/                # 工具函数
│   │   └── views/                # 页面组件
│   └── package.json
├── init.sql                      # 数据库初始化脚本
├── OpenAPI接口.json              # API接口文档
└── README.md                     # 项目说明文档
```

## 🚀 快速开始

### 环境要求
- **JDK**: 17+
- **Node.js**: 20.19.0+ 或 22.12.0+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 后端部署

1. **克隆项目**
   ```bash
   git clone https://github.com/yourusername/MyImageHost.git
   cd MyImageHost
   ```

2. **创建数据库**
   ```bash
   mysql -u root -p < init.sql
   ```

3. **配置后端**
   
   编辑 `myimagehost-backend/src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/imagehost?useSSL=false&serverTimezone=Asia/Shanghai
       username: your_username
       password: your_password
     mail:
       host: smtp.example.com
       username: your_email@example.com
       password: your_email_password
   ```

4. **启动后端**
   ```bash
   cd myimagehost-backend
   mvn clean install
   mvn spring-boot:run
   ```
   
   后端将在 `http://localhost:8080` 运行

### 前端部署

1. **安装依赖**
   ```bash
   cd myimagehost-frontend
   npm install
   ```

2. **配置API地址**
   
   编辑 `src/utils/request.ts`，确保 `baseURL` 指向后端地址

3. **启动前端**
   ```bash
   npm run dev
   ```
   
   前端将在 `http://localhost:5173` 运行

4. **生产构建**
   ```bash
   npm run build
   ```

### 默认账号

首次使用需要注册账号。如需创建管理员账号，请直接在数据库中将用户的 `role` 字段设置为 `ADMIN`。

## 📚 API文档

详细的API接口文档请参考 `OpenAPI接口.json` 文件。

### 主要接口

#### 账户相关
- `POST /auth/login` - 用户登录
- `POST /auth/register` - 用户注册
- `GET /auth/email/verification` - 获取邮箱验证码
- `POST /auth/forgetPassword` - 忘记密码

#### 用户端接口
- `POST /user/upload` - 上传图片
- `GET /user/listImage` - 分页查询图片列表
- `GET /user/searchImages` - 按别名搜索图片
- `PUT /user/updateAlias` - 修改图片别名
- `DELETE /user/deleteImageBatch` - 批量删除图片
- `GET /user/quota` - 获取用户配额信息

#### 管理端接口
- `GET /admin/list` - 查询用户列表
- `POST /admin/searchUser` - 搜索用户
- `GET /admin/getUser/{id}` - 根据ID获取用户
- `POST /admin/disableUser` - 禁用用户账号
- `PUT /admin/enableUser/{id}` - 启用用户
- `DELETE /admin/deleteUser/{id}` - 删除用户
- `POST /admin/updateSpace` - 修改用户空间配额

## 🎨 截图预览

### 用户仪表板
- 配额信息展示
- 图片上传界面
- 图片列表管理

### 管理员后台
- 用户管理界面
- 配额调整功能
- 用户搜索和筛选

## 🔧 配置说明

### 后端配置文件

**application.yml** - 主配置文件
```yaml
spring:
  profiles:
    active: dev  # 开发环境使用 dev，生产环境使用 prod
```

**application-dev.yml** - 开发环境配置
- 数据库连接配置
- 邮件服务配置
- 文件上传路径配置

**application-prod.yml** - 生产环境配置
- 生产数据库配置
- 生产邮件服务配置
- 生产文件存储配置

### 前端配置

**vite.config.ts** - Vite构建配置
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

## 📝 开发计划

- [ ] 支持更多图片格式
- [ ] 添加图片压缩功能
- [ ] 支持图片水印
- [ ] CDN加速支持
- [ ] 图片统计分析
- [ ] 移动端适配
- [ ] Docker容器化部署
- [ ] 对象存储支持（OSS/S3）

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建新的分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 🙏 致谢

感谢以下开源项目：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [MyBatis-Plus](https://baomidou.com/)
- [Sa-Token](https://sa-token.cc/)

---

<div align="center">

**如果这个项目对你有帮助，请给一个 ⭐️ Star 支持一下！**

Made with ❤️ by Ezzi

</div>
