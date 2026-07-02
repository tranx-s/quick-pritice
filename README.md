# 极简公考速刷闯关小程序

一款纯工具、零广告骚扰、零课程的公考行测刷题小程序，叠加轻量化闯关游戏玩法。

## 项目结构

```
programe03-quick-pritice/
├── quick-pritice-app/          # 微信小程序前端
│   ├── pages/
│   │   ├── index/              # 首页
│   │   ├── practice/           # 极速速刷
│   │   ├── game/               # 刷题闯关
│   │   └── profile/            # 个人中心
│   ├── app.js
│   ├── app.json
│   └── app.wxss
│
└── quick-pritice-next/         # Spring Boot 后端
    ├── src/
    │   └── main/
    │       ├── java/com/quickpractice/
    │       │   ├── entity/     # 实体类
    │       │   ├── mapper/     # MyBatis Mapper
    │       │   ├── service/    # 业务逻辑
    │       │   ├── controller/ # 控制器
    │       │   ├── config/     # 配置类
    │       │   └── common/     # 公共类
    │       └── resources/
    │           ├── sql/        # 数据库脚本
    │           └── application.yml
    └── pom.xml
```

## 技术栈

### 前端
- 原生微信小程序

### 后端
- Spring Boot 3.2.5
- JDK 17
- MyBatis-Plus 3.5.6
- MySQL 8.0
- Redis

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库并导入表结构
mysql -u root -p < quick-pritice-next/src/main/resources/sql/schema.sql

# 导入测试数据
mysql -u root -p quick_practice < quick-pritice-next/src/main/resources/sql/data.sql
```

### 2. 后端配置

修改 `quick-pritice-next/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: your_mysql_username
    password: your_mysql_password

wx:
  miniapp:
    appid: your_wechat_appid
    secret: your_wechat_secret
```

### 3. 启动后端

```bash
cd quick-pritice-next
mvn spring-boot:run
```

后端服务运行在 `http://localhost:8080`

### 4. 配置小程序

修改 `quick-pritice-app/app.js` 中的 baseUrl：

```javascript
globalData: {
  baseUrl: 'http://localhost:8080/api'  // 开发环境
  // baseUrl: 'https://your-domain.com/api'  // 生产环境
}
```

### 5. 运行小程序

1. 使用微信开发者工具打开 `quick-pritice-app` 目录
2. 填入你的小程序 AppID
3. 点击编译运行

## 核心功能

### Phase 1 - 已完成 ✅
- [x] 后端项目骨架搭建
- [x] 数据库表结构创建
- [x] 用户登录模块（微信授权）
- [x] 题目管理 CRUD 接口
- [x] 极速速刷模式（前后端联调）
- [x] 小程序基础页面结构

### Phase 2 - 开发中 🚧
- [ ] 专项刷题功能
- [ ] 错题本系统
- [ ] 收藏功能
- [ ] 闯关爬塔游戏
- [ ] 60秒竞速游戏
- [ ] 排行榜

### Phase 3 - 待开发 📋
- [ ] 会员系统
- [ ] 微信支付接入
- [ ] 激励视频广告
- [ ] 裂变奖励体系
- [ ] 后台管理系统

## API 接口文档

### 用户相关

#### 登录
```
POST /api/user/login
Body: { "code": "微信登录code" }
Response: { "code": 200, "data": { "openid": "...", "isVip": false, ... } }
```

#### 更新用户信息
```
POST /api/user/update
Body: { "openid": "...", "nickname": "...", "avatar": "..." }
```

#### 获取用户信息
```
GET /api/user/info?openid=xxx
```

### 题目相关

#### 随机获取题目
```
GET /api/question/random?count=10&moduleType=言语理解
```

#### 提交答题
```
POST /api/question/submit
Body: { "openid": "...", "questionId": 1, "userAnswer": "A" }
Response: { "code": 200, "data": { "isCorrect": true, "correctAnswer": "A", "analysis": "..." } }
```

## 开发计划

按照 PRD 文档，当前优先完成核心功能：
1. ✅ 用户登录和基础架构
2. ✅ 极速速刷（10题随机）
3. 🚧 专项刷题（五大模块）
4. 📋 错题本自动收录
5. 📋 闯关游戏（爬塔 + 竞速）
6. 📋 会员体系和变现

## 注意事项

1. **微信小程序 AppID 和 Secret**：需要在微信公众平台注册小程序后获取
2. **服务器域名配置**：小程序正式上线需要配置合法域名（HTTPS）
3. **激励视频广告**：需要开通微信流量主资格（1000+ 独立访客）

## License

MIT
