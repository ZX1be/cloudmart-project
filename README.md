# CloudMart 电商与秒杀系统

> **项目定位：** Spring Cloud 微服务电商练习 / 高并发秒杀技术验证项目。
> **代码版本：** Maven 根工程声明为 `1.0.0`。截至 **2026-09-29**，本次将审阅通过的后续开发源码及项目文档纳入 Git；本文不把未经运行验收的功能描述成生产能力。

[项目现状与风险分析](docs/项目现状.md) · [下一步开发计划](docs/下一步开发计划.md)

## 项目概览

CloudMart 包含用户认证、商品、购物车/订单、优惠券和秒杀等业务模块。它使用 Redis 做商品缓存和秒杀预扣库存，以 RabbitMQ 将部分下单/库存处理异步化，并由 Spring Cloud Gateway 提供统一路由、JWT 校验和限流入口。

**当前应将其视为开发/验证阶段项目，而不是已具备企业级生产保障的秒杀平台。** 高并发系统不仅需要请求入口快速，还必须证明库存、订单、消息和补偿状态在重复投递、进程崩溃、依赖超时及恢复场景下最终一致。当前代码已有部分相应实现，但缺少足够自动化测试、运行验证和运维指标来形成生产级保证。

## 能力概览

以下代表代码中可见的模块和接口；不等同于本次已构建、部署或完成端到端验收。

| 领域 | 当前代码能力概览 |
|---|---|
| 用户与认证 | 注册、登录、刷新令牌；BCrypt 密码编码；用户资料与收货地址管理；JWT 工具及网关过滤器 |
| 商品 | 商品分页/详情、分类树、评价、收藏、管理端商品和分类接口；商品服务含 Redis 缓存逻辑 |
| 交易 | 购物车、订单创建/查询、支付状态操作、取消、发货/收货；优惠券领取与查询 |
| 秒杀 | 活动查询与管理入口、Redis 库存预热、Lua 原子扣减、用户参与标记、RabbitMQ 异步建单入口 |
| 微服务基础 | 统一响应与异常处理、MyBatis-Plus、Nacos 服务发现、Gateway 路由/JWT/限流、Docker Compose 开发环境 |
| 本次纳入版本的开发内容 | Outbox 共用发布基础设施、普通订单库存消息消费者、管理端优惠券/订单/秒杀接口及部分前端页面；功能与验收边界见[现状文档](docs/项目现状.md) |

> 秒杀用户标记与 Lua 库存扣减目前是两次 Redis 操作；秒杀消息仍使用直接发送路径。具体一致性边界和未关闭风险请阅读[项目现状](docs/项目现状.md)，不要仅凭“Lua + MQ”推断全链路可靠。

## 架构与模块

```text
浏览器 / JMeter
       │
       ▼
前端 Vue / Nginx ──► Gateway :8080 ──► Auth :8081
                       │               Product :8082
                       │               Order :8083
                       │
          ┌────────────┼─────────────┐
          ▼            ▼             ▼
        Nacos       MySQL 8        Redis 6.2
                         ▲            ▲
                         └── RabbitMQ 3.13 ──┘
```

当前 Compose 把所有服务和依赖放在单机开发环境中，应用服务连接同一 MySQL 实例/`cloudmart` 数据库；这不是数据库按服务隔离的生产部署拓扑。`depends_on` 只表达容器启动顺序，不代表 MySQL、Nacos、Redis 或 RabbitMQ 已健康可用。

| 模块 | 主要职责 | Compose 端口 |
|---|---|---:|
| `cloudmart-common` | 通用响应、异常、JWT、DTO、消息及共享组件 | — |
| `cloudmart-gateway` | API 路由、JWT 过滤、用户上下文透传、限流 | 8080 |
| `cloudmart-auth` | 注册登录、用户和地址 | 8081 |
| `cloudmart-product` | 商品、分类、评价、收藏、商品库存 | 8082 |
| `cloudmart-order` | 购物车、订单、优惠券、秒杀 | 8083 |
| `cloudmart-frontend` | Vue 单页应用，经 Nginx 提供页面 | 80 |
| 基础设施 | Nacos、MySQL、Redis、RabbitMQ 管理界面 | 8848/9848、3307、6379、5672/15672 |

### 主要请求路径（按现有代码结构）

- **读商品：** 前端 → Gateway → Product；商品详情/分类树包含 Redis 缓存路径。
- **普通下单：** 前端 → Gateway → Order；Order 与 Product 之间存在库存处理与异步消息相关代码。当前代码包含 Outbox 发布机制；普通订单库存事件已接入，秒杀首条消息仍走直接发送，详见[现状文档](docs/项目现状.md)。
- **秒杀：** Order 查询活动 → Redis 用户标记 → Lua 预扣库存 → RabbitMQ → 异步创建订单。发送失败和消费失败会尝试回补 Redis，但回滚、重投/死信和数据库提交的边界仍需故障注入验证。

## 技术栈

| 层次 | 技术 / 版本（以项目配置为准） |
|---|---|
| 后端运行时 | Java 17、Spring Boot 3.2.7、Spring Cloud 2023.0.3、Spring Cloud Alibaba 2023.0.1.2 |
| 服务治理 | Spring Cloud Gateway、Nacos 2.1.0 |
| 数据访问 | MyBatis-Plus 3.5.7、MySQL 8.0 |
| 缓存与消息 | Redis 6.2、RabbitMQ 3.13；工程中声明 Redisson 3.27.2 |
| 安全与接口文档 | JWT（JJWT 0.12.6）、BCrypt、Knife4j 4.5.0 |
| 前端 | Vue 3、TypeScript、Vite、Element Plus、Pinia、Axios、Vue Router |
| 本地编排 | Docker Compose；前端容器使用 Node 构建并由 Nginx 提供静态文件 |

声明依赖不代表每个依赖都已在核心链路中落地使用；例如工程引入 Redisson 不等于秒杀链路已经采用分布式锁。

## 目录结构与仓库边界

```text
cloudmart/                              # 当前 Git 根目录 / Maven 根工程
├── README.md
├── docs/
│   ├── 项目现状.md
│   └── 下一步开发计划.md
├── pom.xml
├── cloudmart-common/
├── cloudmart-gateway/
├── cloudmart-auth/
├── cloudmart-product/
├── cloudmart-order/
└── cloudmart-frontend/

cloudmart-project/                      # Git 仓库的父级本地工作区
├── deploy.ps1
├── cloudmart-env/docker-compose.yml
└── sql/
    ├── init.sql
    └── migration/
```

**注意：** 实际 Git 根目录是本 README 所在的 `cloudmart/`。部署脚本、Compose 和 SQL 目前仍位于 Git 仓库父目录，尚未纳入远端版本；工作区父目录也保留 README 与两份文档的副本。GitHub 上以本目录内的 README 和 `docs/` 为版本化文档。

## 本地开发与启动

### 环境要求

- Windows PowerShell（当前 `deploy.ps1` 面向 PowerShell）
- Docker Desktop / Docker Compose
- JDK 17、Maven；脚本默认尝试 `E:\Tools\JDK17`，找不到时回退到系统 `java` / `mvn`
- 单独运行前端开发服务器时需要 Node.js 与 npm

### 一键构建并启动

在仓库父目录（含 `deploy.ps1` 的工作区根目录）执行：

```powershell
.\deploy.ps1
```

脚本会在 `cloudmart/` 下执行 `mvn -q -DskipTests clean package`，再进入 `cloudmart-env/` 执行 `docker compose up -d --build`。也支持：

```powershell
.\deploy.ps1 -FrontendOnly   # 仅重建并启动前端
.\deploy.ps1 -NoBuild        # 跳过 Maven；要求所需后端 JAR 已存在
.\deploy.ps1 -JdkHome 'D:\Tools\JDK17'
```

`-FrontendOnly` / `-NoBuild` 仍会调用 Compose。Maven 命令显式跳过测试，因此脚本成功不能替代自动化测试或链路验收。Compose 的依赖项也没有配置健康检查/就绪探测，首次启动时如服务连接失败，需要先检查容器健康状态和日志。

### 前端单独开发

```powershell
cd .\cloudmart\cloudmart-frontend
npm ci
npm run dev
```

生产构建脚本为 `npm run build`（含 `vue-tsc -b` 和 Vite 构建）；本轮 `npm run build` 成功；Vite 提示有大体积 bundle 和配置兼容性警告，未做性能优化。

### 访问入口

- 前端：`http://localhost`
- API 网关：`http://localhost:8080`
- Nacos：`http://localhost:8848`
- RabbitMQ 管理界面：`http://localhost:15672`

其他基础设施与服务端口见上表。Compose 当前映射多个内部服务端口到宿主机，只适用于受控的本地开发网络；不要直接照搬到公网/生产环境。

### 数据初始化提示

Compose 将 `sql/init.sql` 挂载到 MySQL 初始化目录。MySQL 官方容器只会在数据目录首次初始化时执行该脚本；已有 `mysql_data` 卷时，修改 `init.sql` 不会自动升级数据库。已有库应使用经审阅的迁移脚本，并在迁移前备份和核验 schema。当前仓库还没有可确认的统一迁移执行流程。

## 测试与性能数据

- 当前未发现完整的后端 JUnit 单元/集成测试套件；部署脚本默认 `-DskipTests`。
- 盘点初期在 `cloudmart/loadtest/` 下发现 JMeter 报告和数据，但最终只读校验时该目录已为空；其间发生变化的原因无法从当前工作区确认。本 README 不将这些历史材料作为可用或已复现的容量承诺，详见[现状分析](docs/项目现状.md)。
- 盘点初期读到的报告记录的是 JMeter 与全部服务同机的开发机结果，并提醒接口 QPS 不等于成功下单 TPS；报告当前已不可用，详情及验证限制见[现状分析](docs/项目现状.md)。
- 企业级验收至少需要自动化并发正确性测试、消息重复/延迟/丢失测试、依赖故障与恢复测试、独立压测机容量测试，以及可观测的成功订单吞吐和库存对账。

## 安全与生产使用警告

Compose 中存在仅供本地开发的简化凭证和端口映射。**不要把仓库中的开发凭证用于生产。** 投产前还需完成密钥外置与轮换、管理接口角色鉴权审计、网关信任边界、内部服务隔离、TLS、数据库最小权限、消息账号权限、限流策略验证、日志脱敏与审计等工作。具体优先级见[下一步开发计划](docs/下一步开发计划.md)。

## 文档索引

- [项目现状与风险分析](docs/项目现状.md)：架构事实、工作区状态、高并发链路分析、已知风险与验证边界。
- [下一步开发计划](docs/下一步开发计划.md)：按优先级排列的工程化路线及验收标准。
