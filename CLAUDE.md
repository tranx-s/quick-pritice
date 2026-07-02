# 项目配置

## 语言

始终使用中文回复用户。

## Bash 权限

本项目已配置宽松的 Bash 执行权限，绝大多数 shell 命令无需二次确认，直接执行即可。

涵盖范围包括但不限于：

- 文件操作：ls、cat、cp、mv、mkdir、rm、find、touch、chmod、chown
- 文本处理：grep、sed、awk、sort、uniq、wc、head、tail、cut、tr、diff
- 进程管理：ps、kill、pkill、top、htop、jobs、bg、fg
- 网络工具：curl、wget、ping、nslookup、netstat、ss、lsof
- 包管理：npm、pip、pip3、yarn、pnpm、cargo、go、mvn、gradle
- 版本控制：git（所有子命令）
- 构建/运行：make、python、python3、node、java、uvicorn、gunicorn、pytest、jest
- 系统信息：uname、whoami、env、printenv、df、du、free、uptime、date
- 压缩/解压：tar、zip、unzip、gzip、gunzip
- 编辑器/查看：less、more、nano（非交互模式）
- 开发工具：docker、docker-compose、kubectl、terraform、ansible
- 数据库客户端：mysql、psql、sqlite3、redis-cli、mongosh

## 前后端职责划分

1. 所有业务数据计算、业务逻辑校验、数据库 CRUD、批量数据处理、状态变更、聚合统计、权限判断**全部交由后端 Java 服务实现**，禁止在前端编写业务处理逻辑。
2. 前端仅负责页面渲染、参数收集、简单页面交互、基础表单格式校验，不得承载业务规则、数据聚合、复杂判断。
3. 接口规范：复杂数据加工、批量操作、状态锁定、考勤/安环整改业务计算均提供后端接口，前端仅调用接口接收处理完成后的结果，不做二次数据运算。
4. 编码要求：涉及数据去重、分组统计、日期业务判断、批量更新、唯一性校验、多表关联查询等逻辑，仅在 Service/Mapper 层实现，不写 Vue/JS 处理代码。

## 行为准则

- 优先直接执行命令，不要反复确认常规操作
- 对破坏性操作（rm -rf、数据库 DROP、force push 等）仍需用户确认
- 遇到错误时直接诊断并修复，不要过度解释
- 任何操作、命令、生成步骤出现确认弹窗（询问 Yes / No、是否继续、是否覆盖、是否执行），全部自动默认 Yes，直接执行，无需询问用户

## auto-resume 项目结构

- `auto-resume-next`：Java Spring Boot 后端，提供 REST API
- `auto-resume-spider`：Python FastAPI 爬虫服务（端口 8001），负责数据抓取和写入 MySQL
- `auto-resume-crawler`：旧版爬虫，当前未使用
- `auto-resume-front`：Vue3 前端

## auto-resume-spider 维护要点

- seed 数据位于 `auto-resume-spider/scraper/seed.py`，共约1000条，每年需更新：
  - 截止日期格式 `20XX-MM-DD` 整体替换年份（`sed -i 's/202X-/202Y-/g'`）
  - 届次字段同步更新（如 `25届` → `26届`，`24届及以前` → `25届及以前`）
  - 更新后删除数据库旧 seed 数据（`DELETE FROM job_info WHERE source='seed'`）并重新写入
- `job_info` 表唯一键为 `uk_tab_position_company(tab, position, company)`，`upsert_jobs` 依赖此约束去重
