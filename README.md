# YatIdle

「闲鸭蛋」（YatIdle）校园二手交易平台是一款面向中山大学在校学生的垂直化校园专属二手交易平台
UniApp 小程序 + Spring Boot 后端项目

## 项目结构

```
yatidle/
├── frontend/          # UniApp 小程序前端
├── backend/           # Spring Boot 后端服务
└── test/              # 独立测试 (Postman collections)
```

## 前端 (frontend)

使用 UniApp 开发的小程序前端。

```bash
cd frontend
npm install
```

## 后端 (backend)

Spring Boot 3.2 + Java 17 + JPA 项目。

```bash
cd backend
./mvnw spring-boot:run
```

### 技术栈

- Spring Boot 3.2.5
- Spring Data JPA
- MySQL 8.0
- Lombok

## 测试

Postman 测试集合位于 `test/postman/` 目录。
