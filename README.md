# 校园活动中心

面向校园学生的活动信息聚合、分类搜索与官方入口跳转平台。

## 项目目标

解决校园活动分散在社团群、公众号、朋友圈和论坛中，学生难以集中发现、比较和找到官方报名入口的问题。平台只负责收集、展示、搜索和跳转；报名、签到等流程仍在主办方原有系统中完成。

本项目同时作为可部署的软件作品完成：开发者不负责长期运营活动内容，而是提供系统、部署说明和管理后台，由交付对象自行维护内容。

## 第一版（MVP）

- 活动浏览、分类筛选、搜索与详情查看
- 活动主办方、时间、地点、活动介绍展示
- 官方报名链接或二维码展示与跳转
- 管理员审核活动
- 活动内容的收集与维护

## 技术方向

- 前端：Vue 3 + TypeScript
- 后端：Java 21 + Spring Boot 3.5
- 数据库：H2（本地开箱即用）/ MySQL 8（交付环境）
- 后续按实际需求引入 Redis、Docker 和 Nginx

## 本地运行

先启动后端。项目自带 Maven Wrapper，不需要单独安装 Maven：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

默认使用内存中的 H2 数据库，Flyway 会自动建表并导入 12 条模拟活动。后端启动后可访问：

- 健康检查：`http://localhost:8080/api/public/health`
- 活动列表：`http://localhost:8080/api/public/events`
- 活动详情：`http://localhost:8080/api/public/events/1`

再启动前端：

```bash
cd frontend
npm install
npm run dev
```

前端默认请求 `http://localhost:8080/api/public`。如果后端未启动，页面会自动降级到前端内置的模拟数据；也可通过 `VITE_API_BASE_URL` 修改接口地址。

本地管理页面：

- 管理台：`http://localhost:5173/admin`
- 支持活动列表、状态筛选、新增草稿、编辑、送审、发布和下架。
- 当前管理接口尚未接入登录保护，只能用于本地开发演示，不能直接公开部署。

管理端默认请求 `http://localhost:8080/api/admin`，可通过 `VITE_ADMIN_API_BASE_URL` 修改。

生产构建：

```bash
cd frontend
npm run build
```

使用 MySQL 时，先创建 `campus_event_hub` 数据库，并设置环境变量：

```powershell
$env:DB_USERNAME="你的数据库用户名"
$env:DB_PASSWORD="你的数据库密码"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
```

可通过 `DB_URL` 覆盖默认连接地址。不要把数据库密码提交到 GitHub。

## 项目文档

前期调研、需求边界和技术方案位于 [docs](docs/) 目录。

## 当前状态

已完成第一版 H5 学生端、Spring Boot 活动目录后端、Flyway 数据库迁移、公开 API 和活动管理台。管理台支持活动新增、编辑、送审、发布与下架；当前共有 17 项自动化测试。下一步实现管理员登录保护和分类管理。
